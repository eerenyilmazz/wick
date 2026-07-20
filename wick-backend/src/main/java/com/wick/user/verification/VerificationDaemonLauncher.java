package com.wick.user.verification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class VerificationDaemonLauncher implements ApplicationRunner, DisposableBean {

    @Value("${app.verification.script-path}")
    private String scriptPath;

    @Value("${app.internal.secret}")
    private String internalSecret;

    private Process process;

    @Override
    public void run(ApplicationArguments args) {
        File script = new File(scriptPath);
        if (!script.exists()) {
            log.warn("Face verification script not found at '{}', daemon not started", script.getAbsolutePath());
            return;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("python3", script.getAbsolutePath());
            pb.environment().put("INTERNAL_KEY", internalSecret);
            pb.environment().put("PHOTO_DIR", resolvePhotoDir());
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            pb.directory(script.getParentFile());
            process = pb.start();
            log.info("Face verification daemon started (pid={})", process.pid());
        } catch (Exception e) {
            log.error("Failed to start face verification daemon", e);
        }
    }

    @Override
    public void destroy() {
        if (process != null && process.isAlive()) {
            process.destroy();
            log.info("Face verification daemon stopped");
        }
    }

    private String resolvePhotoDir() {
        // face_verify.py default is ./data/photos — resolve relative to script location
        return new File(new File(scriptPath).getParentFile(), "data/photos").getAbsolutePath();
    }
}
