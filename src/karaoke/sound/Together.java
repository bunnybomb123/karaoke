package karaoke.sound;

import java.util.function.Consumer;

public class Together implements Music {

    private Music m1;
    private Music m2;

    public Together(Music m1, Music m2) {
        this.m1 = m1;
        this.m2 = m2;
    }

    /*public <R> R accept(Visitor<R> visitor) {
        // TODO Auto-generated method stub
        return visitor.on(this);
    }*/

    public double duration() {
        // TODO Auto-generated method stub
        return Math.max(m1.duration(), m2.duration());
    }

    public void play(SequencePlayer player, double atBeat, Consumer<String> lyricConsumer) {
        // TODO Auto-generated method stub

    }

}
