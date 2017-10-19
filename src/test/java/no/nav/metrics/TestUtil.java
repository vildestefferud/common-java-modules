package no.nav.metrics;

import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.nav.metrics.handlers.SensuHandler.SENSU_CLIENT_PORT;

public class TestUtil {

    public static <T> T lagAspectProxy(T target, Object aspect) {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        return factory.getProxy();
    }

    public static void resetMetrics() {
        try {
            // Lukk øynene
            Field metricsClient = MetricsFactory.class.getDeclaredField("metricsClient");
            metricsClient.setAccessible(true);

            // Fjerner final lol
            Field modifier = Field.class.getDeclaredField("modifiers");
            modifier.setAccessible(true);
            modifier.setInt(metricsClient, metricsClient.getModifiers() & ~Modifier.FINAL);

            metricsClient.set(null, new MetricsClient());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

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

    public static int getSensuClientPort() {
        return Integer.parseInt(System.getProperty(
                SENSU_CLIENT_PORT,
                "0" // tilfelding port som default
        ));
    }
}
