package no.nav.common.metrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestUtil {

    public static List<String> lesUtAlleMeldingerSendtPaSocket(ServerSocket serverSocket) throws IOException {
        List<String> meldinger = new ArrayList<>();

        String linje = lesLinjeFraSocket(serverSocket);
        while (linje != null) {
            meldinger.addAll(splitStringsFraMelding(linje));
            linje = lesLinjeFraSocket(serverSocket);
        }

        return meldinger;
    }


    public static String lesLinjeFraSocket(ServerSocket serverSocket) throws IOException {
        try {
            serverSocket.setSoTimeout(2000);
            Socket socket = serverSocket.accept();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return bufferedReader.readLine();
        } catch (SocketTimeoutException e) {
            return null;
        }
    }

    public static List<String> splitStringsFraMelding(String melding) {

        int start = melding.indexOf("output\":\"") + 9;
        int end = melding.indexOf("\"", start + 1);

        String output = melding.substring(start, end);

        return Arrays.asList(output.split("\\\\n"));
    }

    public static SensuHandler sensuHandlerForTest(int port) {
        return new SensuHandler(testConfig(port));
    }

    public static SensuConfig testConfig(int port) {
        return SensuConfig.withSensuDefaults(SensuConfig.builder()
                .application("testApp")
                .sensuHost("localhost")
                .sensuPort(port)
                .build()
        );
    }

}
