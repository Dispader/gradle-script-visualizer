package com.nurflugel.gradle.ui;

import static com.nurflugel.gradle.ui.GradleVisualizerUiController.areAllNotNull;

import com.nurflugel.util.gradlescriptvisualizer.domain.Task;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

/** Created with IntelliJ IDEA. User: douglas_bullard Date: 3/8/13 Time: 18:59 To change this template use File | Settings | File Templates. */
public class GradleVisualizerUiControllerTest
{
  @Test(groups = "unit")
  public void testAreAllNotNull() throws Exception
  {
    assertTrue(areAllNotNull(1));
    assertTrue(areAllNotNull(1, 4));
    assertTrue(areAllNotNull(1, "sksksks"));
    assertTrue(areAllNotNull("sksksks"));
    assertTrue(areAllNotNull(new Integer(5)));

    Task task = null;

    assertFalse(areAllNotNull(1, task));
    assertFalse(areAllNotNull(task));
    assertFalse(areAllNotNull(task, task));
    assertFalse(areAllNotNull(task, task, 5));
  }
}
