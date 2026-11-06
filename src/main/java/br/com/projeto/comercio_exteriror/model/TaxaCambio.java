
package br.com.projeto.comercio_exteriror.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "taxas_cambio")
@Getter
@Setter
@NoArgsConstructor
public class TaxaCambio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_taxa")
    private Integer id;

    @Column(name = "moeda", nullable = false, length = 10)
    private String moeda;

    @Column(name = "taxa_media_brl")
    private Double taxaMediaBRL;

    @Column(name = "mes_ano")
    private LocalDate mesAno;
}
