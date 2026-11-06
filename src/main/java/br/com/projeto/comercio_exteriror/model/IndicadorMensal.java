package br.com.projeto.comercio_exteriror.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "indicadores_mensais")
@Getter
@Setter
@NoArgsConstructor
public class IndicadorMensal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_indicador")
    private Integer id;

    @Column(name = "total_operacoes")
    private Integer totalOperacoes;

    @Column(name = "peso_total_kg")
    private Float pesoTotalKg;

    @Column(name = "tempo_medio_trans_dias")
    private Float tempoMedioTransDias;

    @Column(name = "tempo_medio_des_dias")
    private Float tempoMedioDesDias;

    @Column(name = "percentual_atraso")
    private Double percentualAtraso;

    @Column(name = "custo_logistico_total")
    private Double custoLogisticoTotal;

    @Column(name = "mes_ano")
    private LocalDate mesAno;
}
