package bowling.domain;

import bowling.dto.RollDto;
import bowling.exception.RollException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static bowling.asset.Const.PIN_NUM;

public class Roll {
    private static final Map<Integer, Roll> map = new HashMap<>();

    static {
        IntStream.rangeClosed(0, PIN_NUM)
                .forEach(count -> map.put(count, new Roll(count)));
    }

    private final int countOfPins;

    private Roll(int countOfPins) {
        this.countOfPins = countOfPins;
    }

    public static Roll of(int count) {
        if (count < 0 || count > PIN_NUM) {
            throw new RollException("한 프레임에서 쓰러진 핀의 개수는 0 이상 10 이하여야 합니다.");
        }
        return map.get(count);
    }

    int getCountOfPins() {
        return countOfPins;
    }

    RollDto exportRollDto() {
        return new RollDto(countOfPins);
    }
}
