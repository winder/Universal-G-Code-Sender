/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender.model;

import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import java.time.Instant;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wwinder
 */
public class PositionTest {
  
  /**
   * Test of equals method, of class Position.
   */
  @Test
  public void testEquals_Object() {
    System.out.println("equals");
    Position instance = new Position(1, 2, 3, Units.MM);

    Assertions.assertThat(instance.equals(new Position(1, 2, 3, Units.MM)))
            .isTrue();
    Assertions.assertThat(instance.equals(new Position(1, 2, 3, Units.UNKNOWN)))
            .isFalse();

    Assertions.assertThat(instance.equals((Object) new Position(1, 2, 3, Units.MM)))
            .isTrue();
    Assertions.assertThat(instance.equals((Object) new Position(1, 2, 3, Units.UNKNOWN)))
            .isFalse();
  }

  /**
   * Test of isSamePositionIgnoreUnits method, of class Position.
   */
  @Test
  public void testIsSamePositionIgnoreUnits() {
    System.out.println("isSamePositionIgnoreUnits");
    Position instance = new Position(1, 2, 3, Units.MM);

    Assertions.assertThat(instance.isSamePositionIgnoreUnits(new Position(1, 2, 3, Units.MM)))
            .isTrue();
    Assertions.assertThat(instance.isSamePositionIgnoreUnits(new Position(1, 2, 3, Units.UNKNOWN)))
            .isTrue();
  }
}
