import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.testng.Assert.assertTrue;

public class CliE2eTest {

    @Test
    @Parameters({"host","port","user","password","clientJar"})
    public void runJarProactively(String host, @Optional("22") int port, String user, String password, String clientJar) throws Exception {

        Process p = new ProcessBuilder(
                "java",
                "-Dfile.encoding=UTF-8",           // важно, чтобы не ловить «кракозябры»
                "-jar", clientJar
        )
                .redirectErrorStream(true)
                .start();

        StringBuilder log = new StringBuilder();
        Thread reader = new Thread(() -> {
            try (BufferedReader out = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = out.readLine()) != null) {
                    log.append(line).append('\n');
                }
            } catch (IOException ignored) {}
        });
        reader.setDaemon(true);
        reader.start();

        try (BufferedWriter in = new BufferedWriter(
                new OutputStreamWriter(p.getOutputStream(), StandardCharsets.UTF_8))) {

            // даём клиенту стартануть
            Thread.sleep(500);

            sendLine(in, host);
            sendLine(in, String.valueOf(port));
            sendLine(in, user);
            sendLine(in, password);

            // даём клиенту прочитать JSON и показать меню
            Thread.sleep(800);

            sendLine(in, "1");
            Thread.sleep(300);
            sendLine(in, "6");
            in.flush();
        }

        // Жёсткий таймаут на процесс, чтобы CI не висел вечно
        boolean finished = p.waitFor((int) Duration.ofSeconds(30).getSeconds(), java.util.concurrent.TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new AssertionError("CLI timed out. Output so far:\n" + log);
        }

        int code = p.exitValue();
        String out = log.toString();
        assertTrue(out.contains("Connection successful") || out.toLowerCase().contains("success"),
                "Connection error. Log:\n" + out);
        assertTrue(code == 0 || code == 1, "Invalid exit code: " + code + "\nLog:\n" + out);
    }

    private static void sendLine(BufferedWriter in, String s) throws IOException {
        in.write(s);
        in.write('\n');
    }
}
