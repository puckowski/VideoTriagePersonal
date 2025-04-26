package com.keypointforensics.videotriage.audit;

import com.keypointforensics.videotriage.util.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CaseAuditor {

    private static final int LOG_RETRY_ATTEMPTS = 5;
    private static final int WRITE_INTERVAL_SECONDS = 1;
    private static final int RETRY_INTERVAL_SECONDS = 10;

    public static final String CASE_LOG_NAME = "case.txt";
    private static String mCaseLogPath;
    private static boolean mEnabled;

    private static final ConcurrentLinkedQueue<String> mWriteQueue = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<String> mFailedLogQueue = new ConcurrentLinkedQueue<>();
    private static final ConcurrentHashMap<String, Integer> mRetryCountMap = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        scheduler.scheduleAtFixedRate(CaseAuditor::flushWriteQueue, WRITE_INTERVAL_SECONDS, WRITE_INTERVAL_SECONDS, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(CaseAuditor::retryFailedLogs, RETRY_INTERVAL_SECONDS, RETRY_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public static boolean isEnabled() {
        return mEnabled;
    }

    public static void setEnabled(final boolean enabled) {
        mEnabled = enabled;
    }

    public static String getCaseLogPath() {
        return mCaseLogPath;
    }

    public static void setCaseLogPath(final String caseLogPath) {
        final String filePath = File.separator + CASE_LOG_NAME;

        if (caseLogPath.endsWith(filePath)) {
            mCaseLogPath = caseLogPath;
        } else if (!caseLogPath.endsWith(File.separator)) {
            mCaseLogPath = caseLogPath + filePath;
        } else {
            mCaseLogPath = caseLogPath + CASE_LOG_NAME;
        }

        FileUtils.createDirectory(FileUtils.getFileDirectory(mCaseLogPath), false);
    }

    public static void log(final LogLevel level, final String filename, final String text) {
        if (!mEnabled) {
            return;
        }

        if (mCaseLogPath == null) {
            throw new IllegalStateException("Case log path is not set.");
        }

        if (filename == null || filename.isEmpty()) {
            log(level, text);

            return;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = "[" + timestamp + "] [" + level + "][ " + filename + "] " + text;

        mWriteQueue.offer(logEntry);
    }

    public static void log(final LogLevel level, final String text) {
        if (!mEnabled) {
            return;
        }

        if (mCaseLogPath == null) {
            throw new IllegalStateException("Case log path is not set.");
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = "[" + timestamp + "] [" + level + "][Global] " + text;

        mWriteQueue.offer(logEntry);
    }

    private static void flushWriteQueue() {
        if (mCaseLogPath == null) {
            return;
        }

        String logEntry;
        while ((logEntry = mWriteQueue.poll()) != null) {
            if (!writeWithRetries(logEntry)) {
                mFailedLogQueue.offer(logEntry);
                mRetryCountMap.put(logEntry, 1);
            }
        }
    }

    private static boolean writeWithRetries(String logEntry) {
        int attempts = 0;
        while (attempts < LOG_RETRY_ATTEMPTS) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(mCaseLogPath, true))) {
                writer.write(logEntry);
                writer.newLine();
                writer.flush();
                writer.close();
                return true;
            } catch (IOException e) {
                attempts++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    public static void retryFailedLogs() {
        if (mCaseLogPath == null) {
            return;
        }

        String logEntry;
        while ((logEntry = mFailedLogQueue.poll()) != null) {
            if (writeWithRetries(logEntry)) {
                mRetryCountMap.remove(logEntry);
            } else {
                int retries = mRetryCountMap.getOrDefault(logEntry, 1);
                if (retries >= LOG_RETRY_ATTEMPTS) {
                    mRetryCountMap.remove(logEntry);
                } else {
                    mRetryCountMap.put(logEntry, retries + 1);
                    mFailedLogQueue.offer(logEntry);
                }
            }
        }
    }
}
