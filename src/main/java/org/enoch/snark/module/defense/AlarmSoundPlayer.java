package org.enoch.snark.module.defense;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.instance.Instance;

import java.io.*;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import static org.enoch.snark.module.defense.DefenseThread.ALARM;

public class AlarmSoundPlayer implements LineListener {

    public static final String MISSING_WAV = "missing.wav";
    private static AlarmSoundPlayer INSTANCE;
    static boolean shouldPlayAlarm = false;

    private AlarmSoundPlayer() {
    }

    static AlarmSoundPlayer getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new AlarmSoundPlayer();
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
        AlarmSoundPlayer alarmSoundPlayer = new AlarmSoundPlayer();
        shouldPlayAlarm = true;
        alarmSoundPlayer.play();
    }

    private void play() {
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
        String alarmPath = Instance.getConfigMap(DefenseThread.threadName).getConfig(ALARM, MISSING_WAV);
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