package no.nav.sbl.dialogarena.common.kodeverk;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class KodeverkElement {

    private String skjemaNummer;
    private Map<Kodeverk.Nokkel, String> koder;

    KodeverkElement(String skjemaNummer, String gosysId, String tema, String beskrivelseNoekkel, String tittel, String url) {
        this.skjemaNummer = skjemaNummer;
        koder = new HashMap<>();
        koder.put(Kodeverk.Nokkel.BESKRIVELSE, beskrivelseNoekkel);
        koder.put(Kodeverk.Nokkel.GOSYS_ID, gosysId);
        koder.put(Kodeverk.Nokkel.TEMA, tema);
        koder.put(Kodeverk.Nokkel.TITTEL, tittel);
        koder.put(Kodeverk.Nokkel.URL, url);
    }

    Map<Kodeverk.Nokkel, String> getKoderMap() {
        return Collections.unmodifiableMap(koder);
    }

//    public String getSkjemaNummer() {
//        return skjemaNummer;
//    }



}
