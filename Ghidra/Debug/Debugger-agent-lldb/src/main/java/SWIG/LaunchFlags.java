/* ###
 * IP: Apache License 2.0 with LLVM Exceptions
 */
package SWIG;


/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */


public final class LaunchFlags {
  public final static LaunchFlags eLaunchFlagNone = new LaunchFlags("eLaunchFlagNone", lldbJNI.eLaunchFlagNone_get());
  public final static LaunchFlags eLaunchFlagExec = new LaunchFlags("eLaunchFlagExec", lldbJNI.eLaunchFlagExec_get());
  public final static LaunchFlags eLaunchFlagDebug = new LaunchFlags("eLaunchFlagDebug", lldbJNI.eLaunchFlagDebug_get());
  public final static LaunchFlags eLaunchFlagStopAtEntry = new LaunchFlags("eLaunchFlagStopAtEntry", lldbJNI.eLaunchFlagStopAtEntry_get());
  public final static LaunchFlags eLaunchFlagDisableASLR = new LaunchFlags("eLaunchFlagDisableASLR", lldbJNI.eLaunchFlagDisableASLR_get());
  public final static LaunchFlags eLaunchFlagDisableSTDIO = new LaunchFlags("eLaunchFlagDisableSTDIO", lldbJNI.eLaunchFlagDisableSTDIO_get());
  public final static LaunchFlags eLaunchFlagLaunchInTTY = new LaunchFlags("eLaunchFlagLaunchInTTY", lldbJNI.eLaunchFlagLaunchInTTY_get());
  public final static LaunchFlags eLaunchFlagLaunchInShell = new LaunchFlags("eLaunchFlagLaunchInShell", lldbJNI.eLaunchFlagLaunchInShell_get());
  public final static LaunchFlags eLaunchFlagLaunchInSeparateProcessGroup = new LaunchFlags("eLaunchFlagLaunchInSeparateProcessGroup", lldbJNI.eLaunchFlagLaunchInSeparateProcessGroup_get());
  public final static LaunchFlags eLaunchFlagDontSetExitStatus = new LaunchFlags("eLaunchFlagDontSetExitStatus", lldbJNI.eLaunchFlagDontSetExitStatus_get());
  public final static LaunchFlags eLaunchFlagDetachOnError = new LaunchFlags("eLaunchFlagDetachOnError", lldbJNI.eLaunchFlagDetachOnError_get());
  public final static LaunchFlags eLaunchFlagShellExpandArguments = new LaunchFlags("eLaunchFlagShellExpandArguments", lldbJNI.eLaunchFlagShellExpandArguments_get());
  public final static LaunchFlags eLaunchFlagCloseTTYOnExit = new LaunchFlags("eLaunchFlagCloseTTYOnExit", lldbJNI.eLaunchFlagCloseTTYOnExit_get());
  public final static LaunchFlags eLaunchFlagInheritTCCFromParent = new LaunchFlags("eLaunchFlagInheritTCCFromParent", lldbJNI.eLaunchFlagInheritTCCFromParent_get());

  public final int swigValue() {
    return swigValue;
  }

  public String toString() {
    return swigName;
  }

  public static LaunchFlags swigToEnum(int swigValue) {
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue)
        return swigValues[i];
    throw new IllegalArgumentException("No enum " + LaunchFlags.class + " with value " + swigValue);
  }

  private LaunchFlags(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private LaunchFlags(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue+1;
  }

  private LaunchFlags(String swigName, LaunchFlags swigEnum) {
    this.swigName = swigName;
    this.swigValue = swigEnum.swigValue;
    swigNext = this.swigValue+1;
  }

  private static LaunchFlags[] swigValues = { eLaunchFlagNone, eLaunchFlagExec, eLaunchFlagDebug, eLaunchFlagStopAtEntry, eLaunchFlagDisableASLR, eLaunchFlagDisableSTDIO, eLaunchFlagLaunchInTTY, eLaunchFlagLaunchInShell, eLaunchFlagLaunchInSeparateProcessGroup, eLaunchFlagDontSetExitStatus, eLaunchFlagDetachOnError, eLaunchFlagShellExpandArguments, eLaunchFlagCloseTTYOnExit, eLaunchFlagInheritTCCFromParent };
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}
