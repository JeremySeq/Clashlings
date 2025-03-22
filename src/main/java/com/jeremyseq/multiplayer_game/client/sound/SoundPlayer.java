package com.jeremyseq.multiplayer_game.client.sound;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

public class SoundPlayer {

    public static class Sounds {
        public static final String PLAYER_ATTACK = "/sounds/player_attack.wav";
        public static final String ENEMY_HURT = "/sounds/enemy_hurt.wav";
        public static final String ENEMY_DEATH = "/sounds/enemy_death.wav";
        public static final String PLAYER_FOOTSTEP = "/sounds/player_footstep.wav";
    }

    public static void playSound(String filePath, float volume) {
        try {
            Clip clip = loadSound(filePath);
            if (clip != null) {
                setVolume(clip, volume);

                clip.setFramePosition(0); // reset to the start of clip
                clip.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playSound(String filePath) {
        playSound(filePath, 1);
    }

    private static void setVolume(Clip clip, float volume) {
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        if (gainControl != null) {
            float minGain = gainControl.getMinimum();
            float maxGain = gainControl.getMaximum();
            float gain = minGain + (maxGain - minGain) * volume;
            gainControl.setValue(gain);
        }
    }

    private static Clip loadSound(String filePath) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        try (InputStream audioSrc = SoundPlayer.class.getResourceAsStream(filePath)) {
            if (audioSrc == null) {
                System.err.println("Sound file not found: " + filePath);
                return null;
            }
            AudioInputStream originalStream = AudioSystem.getAudioInputStream(audioSrc);

            // Convert the audio format to PCM 16-bit, 44.1kHz, stereo
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,  // PCM encoding
                    44100, // Sample rate
                    16,    // Sample size (in bits)
                    2,     // Channels (stereo)
                    4,     // Frame size (in bytes)
                    44100, // Frame rate (in Hz)
                    false  // Little-endian
            );

            AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, originalStream);

            Clip clip = AudioSystem.getClip();
            clip.open(convertedStream);
            return clip;
        }
    }
}
