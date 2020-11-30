package bowling.domain;

import bowling.dto.PlayerStatusDto;

import java.util.List;
import java.util.function.Function;

class PlayerStatus {
    private final RollSubject subject;
    private final Board board;

    private PlayerStatus(RollSubject subject, Board board) {
        this.subject = subject;
        this.board = board;
    }

    static PlayerStatus of(Function<Integer, Roll> rollGenerator, List<Observer> rollObservers) {
        Board board = new Board();
        RollSubject subject = new RollSubject(() -> rollGenerator.apply(board.frameNo()));
        subject.register(new BoardObserver(board));
        rollObservers.forEach(subject::register);
        return new PlayerStatus(
                subject,
                board
        );
    }

    void playFrame() {
        subject.execute();
        if (!board.isFrameFinished()) {
            subject.execute();
        }
    }

    void playBonus() {
        if (board.isSpare()) {
            subject.execute();
        }
        if (board.isStrike()) {
            subject.execute();
            subject.executeZero();
        }
    }

    PlayerStatusDto exportPlayerStatusDto() {
        return new PlayerStatusDto(subject.exportRollsDto(), board.exportBoardDto());
    }
}
