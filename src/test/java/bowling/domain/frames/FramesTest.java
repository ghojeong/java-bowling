package bowling.domain.frames;

import bowling.domain.Pin;
import bowling.domain.Pins;
import bowling.domain.frame.Frame;
import bowling.domain.frame.FrameEnum;
import bowling.dto.FrameDto;
import bowling.exception.BadCountOfPinsException;
import bowling.exception.PinException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;

import static bowling.asset.Const.MAX_FRAME_NO;
import static bowling.domain.frame.FrameEnum.*;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

class FramesTest {
    private Frames frames;
    private Pins pins;

    @BeforeEach
    void setUp() {
        frames = new Frames();
        pins = new Pins();
    }

    private void addRoll(Pin pin) {
        pins.add(pin);
        frames.update(pins);
    }

    private List<FrameEnum> toFrameList(Frames frames) {
        return frames.exportFramesDto()
                .getFrames()
                .stream()
                .map(FrameDto::getFrameEnum)
                .collect(toList());
    }

    @Test
    @DisplayName("sublist 테스트")
    void sublist() {
        addRoll(Pin.of(10));

        addRoll(Pin.of(1));
        addRoll(Pin.of(9));

        addRoll(Pin.of(4));
        addRoll(Pin.of(5));

        addRoll(Pin.of(8));

        assertThat(frames.subList(1, 3)
                .stream()
                .map(Frame::exportFrameDto)
                .map(FrameDto::getFrameEnum)
        ).isEqualTo(Arrays.asList(FrameEnum.SPARE, FrameEnum.MISS));
    }

    @ParameterizedTest
    @DisplayName("핀 갯수가 음수이면, RollException 이 발생한다.")
    @CsvSource(value = {"-8$3", "10$-1", "11$3", "4$12"}, delimiter = '$')
    void scenario_negative(int roll1, int roll2) {
        assertThatExceptionOfType(PinException.class)
                .isThrownBy(() -> {
                    addRoll(Pin.of(roll1));
                    addRoll(Pin.of(roll2));
                }).withMessage("핀의 개수는 0 이상 10 이하여야 합니다.");
    }

    @ParameterizedTest
    @DisplayName("적절하지 않은 핀 갯수이면, BadCountOfPinsException 이 발생한다.")
    @CsvSource(value = {"9$2", "4$7"}, delimiter = '$')
    void scenario_badCountOfPins(int roll1, int roll2) {
        assertThatExceptionOfType(BadCountOfPinsException.class)
                .isThrownBy(() -> {
                    addRoll(Pin.of(roll1));
                    addRoll(Pin.of(roll2));
                }).withMessage("한 프레임에서 쓰러트린 핀의 개수는 0 이상 10 이하여야 합니다.");
    }

    @Test
    @DisplayName("볼링을 치지 않은 시나리오 테스트")
    void scenario_empty() {
        assertAll(
                () -> assertThat(toFrameList(frames))
                        .isEqualTo(emptyList())
        );
    }


    @Test
    @DisplayName("STRIKE 를 두번 치는 시나리오 테스트")
    void scenario_strike() {
        addRoll(Pin.of(10));
        addRoll(Pin.of(10));
        assertAll(
                () -> assertThat(toFrameList(frames))
                        .isEqualTo(Arrays.asList(STRIKE, STRIKE))
        );
    }

    @Test
    @DisplayName("STRIKE, STRIKE, SPARE 를 치는 시나리오 테스트")
    void scenario_strike_spare() {
        addRoll(Pin.of(10));
        addRoll(Pin.of(10));

        addRoll(Pin.of(1));
        addRoll(Pin.of(9));
        assertAll(
                () -> assertThat(toFrameList(frames))
                        .isEqualTo(Arrays.asList(STRIKE, STRIKE, SPARE))
        );
    }

    @Test
    @DisplayName("STRIKE, SPARE, MISS 를 치는 시나리오 테스트")
    void scenario_strike_spare_miss() {
        addRoll(Pin.of(10));

        addRoll(Pin.of(1));
        addRoll(Pin.of(9));

        addRoll(Pin.of(4));
        addRoll(Pin.of(5));
        assertAll(
                () -> assertThat(toFrameList(frames))
                        .isEqualTo(Arrays.asList(STRIKE, SPARE, MISS))
        );
    }

    @Test
    @DisplayName("UNFINISHED 시나리오 테스트")
    void scenario_unfinished() {
        addRoll(Pin.of(10));

        addRoll(Pin.of(1));
        addRoll(Pin.of(9));

        addRoll(Pin.of(4));
        addRoll(Pin.of(5));

        addRoll(Pin.of(10));

        addRoll(Pin.of(8));
        assertAll(
                () -> assertThat(toFrameList(frames))
                        .isEqualTo(Arrays.asList(STRIKE, SPARE, MISS, STRIKE, UNFINISHED))
        );
    }

    @Test
    @DisplayName("STRIKE 100번 추가하는 시나리오 테스트")
    void scenario_strike_100() {
        for (int i = 0; i < 100; i++) {
            addRoll(Pin.of(10));
        }
        List<FrameEnum> frameList = toFrameList(frames);
        assertAll(
                () -> assertThat(frameList)
                        .isEqualTo(Arrays.asList(STRIKE, STRIKE, STRIKE, STRIKE, STRIKE, STRIKE, STRIKE, STRIKE, STRIKE, STRIKE)),
                () -> assertThat(frames.size())
                        .isEqualTo(MAX_FRAME_NO)
        );
    }

    @Test
    @DisplayName("SPARE 100번 추가하는 시나리오 테스트")
    void scenario_spare_100() {
        for (int i = 0; i < 100; i++) {
            addRoll(Pin.of(5));
        }
        List<FrameEnum> frameList = toFrameList(frames);
        assertAll(
                () -> assertThat(frameList)
                        .isEqualTo(Arrays.asList(SPARE, SPARE, SPARE, SPARE, SPARE, SPARE, SPARE, SPARE, SPARE, SPARE)),
                () -> assertThat(frames.size())
                        .isEqualTo(MAX_FRAME_NO)
        );
    }

    @Test
    @DisplayName("MISS 100번 추가하는 시나리오 테스트")
    void scenario_miss_100() {
        for (int i = 0; i < 100; i++) {
            addRoll(Pin.of(3));
        }
        List<FrameEnum> frameList = toFrameList(frames);
        assertAll(
                () -> assertThat(frameList)
                        .isEqualTo(Arrays.asList(MISS, MISS, MISS, MISS, MISS, MISS, MISS, MISS, MISS, MISS)),
                () -> assertThat(frames.size())
                        .isEqualTo(MAX_FRAME_NO)
        );
    }
}
