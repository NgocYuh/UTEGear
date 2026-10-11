package com.utegear.util;
import static org.assertj.core.api.Assertions.assertThat; import org.junit.jupiter.api.Test;
class SlugifierTest { @Test void convertsVietnameseName(){assertThat(Slugifier.slugify("Bàn phím cơ Đỏ")).isEqualTo("ban-phim-co-do");} }
