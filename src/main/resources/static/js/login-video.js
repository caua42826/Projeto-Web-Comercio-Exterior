(() => {
    const background = document.querySelector('.cinematic-background');
    if (!background) return;

    const videos = Array.from(background.querySelectorAll('.cinematic-video'));
    if (videos.length !== 2) {
        console.error('[login-video] Eram esperados dois elementos de vídeo, mas foram encontrados:', videos.length);
        return;
    }

    const FADE_MS = window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 0 : 1250;
    const MAX_FAILURES = 3;
    const failures = [0, 0];
    const unavailable = [false, false];
    let activeIndex = videos.findIndex((video) => video.classList.contains('is-active'));
    let switching = false;
    let queuedEndedIndex = null;

    if (activeIndex < 0) activeIndex = 0;
    videos.forEach((video) => {
        // O ciclo é controlado pelos eventos "ended", não pelo loop nativo.
        video.loop = false;
        video.muted = true;
        video.playsInline = true;
        video.preload = 'auto';
    });

    const describeError = (video) => {
        const mediaError = video.error;
        if (!mediaError) return 'Erro de reprodução sem detalhes do navegador.';
        const messages = {
            1: 'A reprodução foi interrompida.',
            2: 'Falha de rede ao carregar o arquivo.',
            3: 'O navegador não conseguiu decodificar o arquivo.',
            4: 'Formato ou fonte de vídeo não suportados.'
        };
        return `${messages[mediaError.code] || 'Falha de mídia.'} Código: ${mediaError.code}.`;
    };

    const waitForMetadata = (video) => {
        if (video.readyState >= HTMLMediaElement.HAVE_METADATA) return Promise.resolve();
        return new Promise((resolve, reject) => {
            const timeoutId = window.setTimeout(() => {
                cleanup();
                reject(new Error('Tempo limite ao carregar os metadados do vídeo.'));
            }, 20000);
            const cleanup = () => {
                window.clearTimeout(timeoutId);
                video.removeEventListener('loadedmetadata', onReady);
                video.removeEventListener('error', onError);
            };
            const onReady = () => { cleanup(); resolve(); };
            const onError = () => { cleanup(); reject(new Error(describeError(video))); };
            video.addEventListener('loadedmetadata', onReady, { once: true });
            video.addEventListener('error', onError, { once: true });
        });
    };

    const startVideo = async (index, restart) => {
        const video = videos[index];
        if (unavailable[index]) throw new Error('Este vídeo foi desativado após falhas repetidas.');

        for (let attempt = 1; attempt <= MAX_FAILURES; attempt += 1) {
            try {
                if (restart) {
                    await waitForMetadata(video);
                    video.currentTime = 0;
                }
                await video.play();
                failures[index] = 0;
                return;
            } catch (error) {
                failures[index] += 1;
                console.error(`[login-video] Falha ao iniciar o vídeo ${index + 1} (tentativa ${attempt}/${MAX_FAILURES}):`, error);
                if (failures[index] >= MAX_FAILURES) {
                    unavailable[index] = true;
                    console.error(`[login-video] Vídeo ${index + 1} desativado após falhas repetidas:`, describeError(video));
                    throw error;
                }

                video.pause();
                video.load();
                await new Promise((resolve) => window.setTimeout(resolve, attempt * 500));
            }
        }
    };

    const keepAvailableVideoRunning = async (index) => {
        const video = videos[index];
        if (unavailable[index]) return;
        try {
            await startVideo(index, true);
            video.loop = true;
            background.dataset.videoStatus = 'fallback';
            console.warn(`[login-video] O vídeo ${index + 1} será repetido sozinho enquanto o outro arquivo estiver indisponível.`);
        } catch (error) {
            console.error('[login-video] Não foi possível recuperar nenhum plano de fundo de vídeo:', error);
            background.dataset.videoStatus = 'unavailable';
        }
    };

    const switchTo = async (nextIndex, restart = true) => {
        if (switching || nextIndex === activeIndex) return;
        switching = true;
        const previousIndex = activeIndex;
        const previous = videos[previousIndex];
        const next = videos[nextIndex];

        try {
            await startVideo(nextIndex, restart);
            next.classList.add('is-active');
            previous.classList.remove('is-active');
            activeIndex = nextIndex;
            background.dataset.videoStatus = 'playing';
            window.setTimeout(() => {
                if (activeIndex !== previousIndex) previous.pause();
            }, FADE_MS + 100);
        } catch (error) {
            console.error(`[login-video] Não foi possível alternar para o vídeo ${nextIndex + 1}:`, error);
            if (unavailable[nextIndex]) {
                queuedEndedIndex = null;
                await keepAvailableVideoRunning(previousIndex);
            }
        } finally {
            switching = false;
            if (queuedEndedIndex === activeIndex) {
                queuedEndedIndex = null;
                switchTo((activeIndex + 1) % videos.length);
            } else {
                queuedEndedIndex = null;
            }
        }
    };

    videos.forEach((video, index) => {
        video.addEventListener('ended', () => {
            if (switching) {
                queuedEndedIndex = index;
                return;
            }
            if (index !== activeIndex) return;
            switchTo((index + 1) % videos.length);
        });

        video.addEventListener('error', () => {
            console.error(`[login-video] Erro no vídeo ${index + 1}:`, describeError(video), video.currentSrc || 'fonte indisponível');
            if (index === activeIndex && !switching) switchTo((index + 1) % videos.length);
        });
    });

    document.addEventListener('visibilitychange', () => {
        if (document.hidden) return;
        const activeVideo = videos[activeIndex];
        if (activeVideo.ended) {
            switchTo((activeIndex + 1) % videos.length);
        } else if (activeVideo.paused && !unavailable[activeIndex]) {
            startVideo(activeIndex, false).catch((error) => {
                console.error('[login-video] Falha ao retomar a reprodução ao voltar para a página:', error);
                switchTo((activeIndex + 1) % videos.length);
            });
        }
    });

    startVideo(activeIndex, false).then(() => {
        background.dataset.videoStatus = 'playing';
    }).catch(() => {
        const otherIndex = (activeIndex + 1) % videos.length;
        switchTo(otherIndex, true);
    });
})();
