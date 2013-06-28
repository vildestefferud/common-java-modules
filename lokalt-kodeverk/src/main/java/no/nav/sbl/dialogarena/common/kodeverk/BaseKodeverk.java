package no.nav.sbl.dialogarena.common.kodeverk;

import no.nav.modig.core.exception.ApplicationException;

import java.util.HashMap;
import java.util.Map;

import static no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.Nokkel.TITTEL;

/**
 * Baseklasse for implementasjon av kodeverk-interface
 */
abstract class BaseKodeverk implements Kodeverk {

    protected final Map<String, KodeverkElement> dbSkjema = new HashMap<>();

    protected final Map<String, KodeverkElement> dbVedlegg = new HashMap<>();

    @Override
    public boolean isEgendefinert(String skjemaId) {
        return ANNET.equals(skjemaId);
    }

    @Override
    public String getTittel(String skjemaId) {
        return getKode(skjemaId, TITTEL);
    }

    @Override
    public String getKode(String skjemaId, Nokkel nokkel) {
        return getKoder(skjemaId).get(nokkel);
    }

    @Override
    public Map<Nokkel, String> getKoder(String vedleggsIdOrSkjemaId) {
        if (dbSkjema.containsKey(vedleggsIdOrSkjemaId)) {
            return dbSkjema.get(vedleggsIdOrSkjemaId).getKoderMap();
        }
        else
        {
            if (dbVedlegg.containsKey(vedleggsIdOrSkjemaId))
            {
                return dbVedlegg.get(vedleggsIdOrSkjemaId).getKoderMap();
            }
        }
        throw new ApplicationException("\n ---- Fant ikke kodeverk : " + vedleggsIdOrSkjemaId + "---- \n");
    }

}

