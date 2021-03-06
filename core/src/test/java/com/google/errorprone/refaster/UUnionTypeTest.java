/*
 * Copyright 2014 The Error Prone Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.refaster;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.SerializableTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link UUnionType}.
 *
 * @author lowasser@google.com (Louis Wasserman)
 */
@RunWith(JUnit4.class)
public class UUnionTypeTest {
  @Test
  public void equality() {
    new EqualsTester()
        .addEqualityGroup(
            UUnionType.create(
                UClassIdent.create("java.lang.IllegalArgumentException"),
                UClassIdent.create("java.lang.IllegalStateException")))
        .addEqualityGroup(
            UUnionType.create(
                UClassIdent.create("java.lang.IllegalStateException"),
                UClassIdent.create("java.lang.IllegalArgumentException")))
        .addEqualityGroup(
            UUnionType.create(
                UClassIdent.create("java.lang.IllegalStateException"),
                UClassIdent.create("java.lang.IllegalArgumentException"),
                UClassIdent.create("java.lang.IndexOutOfBoundsException")))
        .testEquals();
  }

  @Test
  public void serialization() {
    SerializableTester.reserializeAndAssert(
        UUnionType.create(
            UClassIdent.create("java.lang.IllegalArgumentException"),
            UClassIdent.create("java.lang.IllegalStateException")));
  }
}
