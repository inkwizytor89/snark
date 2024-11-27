package org.enoch.snark.instance.si.module.event;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.si.module.defense.DefenseThread;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

import static org.enoch.snark.instance.si.module.defense.DefenseThread.ALARM;

public class NotificationSoundPlayer implements LineListener {

    public static final String MISSING_WAV = "missing.wav";
    private static NotificationSoundPlayer INSTANCE;
    static boolean shouldPlayAlarm = false;

    private NotificationSoundPlayer() {
    }

    static NotificationSoundPlayer getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new NotificationSoundPlayer();
        }
        return INSTANCE;
    }

    @Override
    public void update(LineEvent event) {
        if (LineEvent.Type.START == event.getType()) {
        } else if (LineEvent.Type.STOP == event.getType()) {
            shouldPlayAlarm = false;
        }
    }

    public static void main(String[] args) {
        NotificationSoundPlayer alarmSoundPlayer = new NotificationSoundPlayer();
        shouldPlayAlarm = true;
        alarmSoundPlayer.play();
        SleepUtil.secondsToSleep(10L);
        shouldPlayAlarm = true;
        alarmSoundPlayer.play();
    }

    public void play() {
        Runnable task = () -> {
            try {
                File soundFile = new File(getAlarmAudioFilePath());
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);

                AudioFormat format = audioStream.getFormat();
                DataLine.Info info = new DataLine.Info(Clip.class, format);

                Clip audioClip = (Clip) AudioSystem.getLine(info);
                audioClip.addLineListener(this);
                audioClip.open(audioStream);
                audioClip.start();
                while (shouldPlayAlarm) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                shouldPlayAlarm = false;
                audioClip.close();
                audioStream.close();

            } catch (UnsupportedAudioFileException | LineUnavailableException | IOException ex) {
                System.out.println("Error occurred during playback process: "+ ex.getMessage());
                ex.printStackTrace();
            }
        };
        new Thread(task).start();
    }

    private String getAlarmAudioFilePath() {
        String alarmPath = "C:\\Users\\Kamil-PC\\Downloads\\snark-beta\\snark-beta\\notification.wav";
//        String alarmPath = Instance.getGlobalMainConfigMap(EventThread.threadType).getConfig(ALARM, MISSING_WAV);
        if(!alarmPath.equals(MISSING_WAV)) return alarmPath;
        File[] files = new File("").listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
        if(files== null || files.length == 0) return null;
        return files[0].getAbsolutePath();
    }

    public static void start() {
        if(!shouldPlayAlarm) {
            System.err.println("Alarm activate");
            shouldPlayAlarm = true;
            getInstance().play();
        }
    }

    public static void stop() {
        shouldPlayAlarm = false;
    }
}