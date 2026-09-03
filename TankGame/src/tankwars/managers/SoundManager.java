package tankwars.managers;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class SoundManager {

        private static Clip musicClip;

        //background music
        public static void playMusic(String filename, float volume) {
            try {
                if (musicClip != null && musicClip.isRunning()) {
                    musicClip.stop();
                    musicClip.close();
                }
                AudioInputStream audio = AudioSystem.getAudioInputStream(Objects.requireNonNull
                        (SoundManager.class.getClassLoader().getResource("sounds/" + filename)));
                musicClip = AudioSystem.getClip();
                musicClip.open(audio);

                FloatControl gain = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(volume);
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                musicClip.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void stopMusic() {
            if (musicClip != null) {
                musicClip.stop();
                musicClip.close();
            }
        }


        //plays a short clip sound effect such as shooting laser or asteriod breaking sounds
        public static void playSound (String fileName, float volume) {
            try {
                URL url = SoundManager.class.getClassLoader().getResource("sounds/" + fileName);

                AudioInputStream audio = AudioSystem.getAudioInputStream(url);

                Clip clip = AudioSystem.getClip();

                clip.open(audio);

                //control the volume of the sound especially the lasers because they were really loud
                FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

                control.setValue(volume);

                clip.start();

           } catch(Exception e){
                 e.printStackTrace();
             }
        }
}