(() => {
    const eyeIcon = `
        <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
            <path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Z"></path>
            <circle cx="12" cy="12" r="3"></circle>
        </svg>`;
    const hiddenIcon = `
        <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
            <path d="m3 3 18 18"></path>
            <path d="M10.6 5.2A10.8 10.8 0 0 1 12 5c6.4 0 10 7 10 7a16.5 16.5 0 0 1-3.1 3.7"></path>
            <path d="M6.2 6.2C3.5 8.1 2 12 2 12s3.6 7 10 7a10.4 10.4 0 0 0 4-.8"></path>
            <path d="M9.9 9.9a3 3 0 0 0 4.2 4.2"></path>
        </svg>`;

    document.querySelectorAll('input[type="password"]').forEach((input, index) => {
        if (input.parentElement.classList.contains('password-input')) return;

        if (!input.id) input.id = `password-field-${index + 1}`;

        const wrapper = document.createElement('div');
        wrapper.className = 'password-input';
        input.parentNode.insertBefore(wrapper, input);
        wrapper.appendChild(input);

        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'password-visibility-toggle';
        button.setAttribute('aria-label', 'Mostrar senha');
        button.setAttribute('aria-controls', input.id);
        button.setAttribute('aria-pressed', 'false');
        button.title = 'Mostrar senha';
        button.innerHTML = eyeIcon;
        button.addEventListener('click', () => {
            const mostrar = input.type === 'password';
            input.type = mostrar ? 'text' : 'password';
            button.setAttribute('aria-label', mostrar ? 'Ocultar senha' : 'Mostrar senha');
            button.setAttribute('aria-pressed', String(mostrar));
            button.title = mostrar ? 'Ocultar senha' : 'Mostrar senha';
            button.innerHTML = mostrar ? hiddenIcon : eyeIcon;
        });
        wrapper.appendChild(button);
    });
})();
