package com.lince.observer.data;

import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.RegistroException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * com.lince.observer.data
 * Class LegacyStatsHelper
 * 14/03/2019
 * TODO ASF24: Refactor to remove javafx dependencies, uses swing packages!
 * @author berto (alberto.soto@gmail.com)
 */
public class LegacyStatsHelper {
    protected static final Logger log = LoggerFactory.getLogger(LegacyStatsHelper.class);

    /**
     * @param register       legacy register
     * @param secondRegister legacy register
     * @return List of kappa index for category
     */
    public static List<Double> getKappaValues(Registro register, Registro secondRegister, List<Object> criterios) {
        List<Double> kappas = null;

        try {
            kappas = new ArrayList<>(criterios.size());
            for (Object obj : (criterios.isEmpty()) ? InstrumentoObservacional.getInstance().getCriterios() : criterios.toArray()) {
                Criterio criterio = (Criterio) obj;
                String[] valores = register.getColumna(criterio);
                String[] otrosValores = secondRegister.getColumna(criterio);
                int tam = valores.length < otrosValores.length ? valores.length : otrosValores.length;
                double acuerdo = LegacyStatsHelper.acuerdo(valores, otrosValores, tam);
                double pe = LegacyStatsHelper.probabilidad(valores, otrosValores, tam);
                double p0 = acuerdo / tam;
                //log.info("calcularKappa - pe"+pe);
                double kappa = pe == 1 ? 1 : (p0 - pe) / (1 - pe);
                kappas.add(kappa);
            }
        } catch (Exception e) {
            log.error("kappas", e);
        }
        return kappas;
    }


    /**
     * Extracted Original method at Lince 1.4
     *
     * @param otroRegistroFile
     * @param criterios
     * @return
     * @throws RegistroException
     */
    public static List<Double> calcularKappa(Registro register, File otroRegistroFile, List<Object> criterios) throws RegistroException {
        List<LegacyToolException> exceptions = new ArrayList<>();
        Registro otroRegistro = Registro.cargarRegistro(otroRegistroFile, exceptions);
        return getKappaValues(register, otroRegistro, criterios);
    }

    /**
     * Notas 2018
     * ----------
     * <p>
     * Se tiene que hacer criterio a criterio
     * O1   O2  Acuerdo
     * ================
     * cab  cab ok
     * cab  pie no
     *
     * @param valores
     * @param otrosValores
     * @param tam
     * @return
     */
    public static int acuerdo(String[] valores, String[] otrosValores, int tam) {
        int acuerdo = 0;
        for (int i = 0; i < tam; i++) {
            if (valores[i].equals(otrosValores[i])) {
                acuerdo++;
            }
        }
        return acuerdo;
    }

    public static double calcularMedia(List<Double> kappas) {
        double d = 0;
        for (Double kappa : kappas) {
            d += kappa;
        }
        return d / kappas.size();
    }

    private static Map<String, Integer> sumarFrecuencia(String[] valores, int tam) {
        Map<String, Integer> frecuencia = new HashMap<String, Integer>();
        for (int i = 0; i < tam; i++) {
            Integer n = frecuencia.get(valores[i]);
            if (n == null) {
                n = 0;
            }
            frecuencia.put(valores[i], n + 1);
        }
        return frecuencia;
    }

    private static double probabilidad(String[] valores, String[] otrosValores, int tam) {
        Map<String, Integer> frecuenciaThis = sumarFrecuencia(valores, tam);
        Map<String, Integer> frecuenciaOtro = sumarFrecuencia(otrosValores, tam);
        Set<String> categorias = new HashSet<String>(frecuenciaThis.keySet());
        categorias.addAll(frecuenciaOtro.keySet());
        double prob = 0;
        double tam2 = tam * tam;
        for (String categoria : categorias) {
            Integer n = frecuenciaThis.get(categoria);
            Integer m = frecuenciaOtro.get(categoria);
            if (n != null && m != null) {
                prob += n * m / tam2;
            }
        }
        return prob;
    }
}
