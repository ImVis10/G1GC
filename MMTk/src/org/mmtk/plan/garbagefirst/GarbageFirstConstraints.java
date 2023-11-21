/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package org.mmtk.plan.garbagefirst;

import org.mmtk.plan.StopTheWorldConstraints;
import org.mmtk.policy.region.Region;
import org.mmtk.policy.region.RegionSpace;
import org.vmmagic.pragma.Uninterruptible;

/**
 * SemiSpace common constants.
 */
@Uninterruptible
public class GarbageFirstConstraints extends StopTheWorldConstraints {
  @Override
  public boolean movesObjects() {
    return true;
  }
  @Override
  public boolean needsForwardAfterLiveness() {
    return false;
  }
  @Override
  public int gcHeaderBits() {
    return RegionSpace.LOCAL_GC_BITS_REQUIRED;
  }
  @Override
  public int gcHeaderWords() {
    return Validation.ENABLED ? 1 : 0;
  }
  @Override
  public int numSpecializedScans() {
    return 3;
  }
  @Override
  public int maxNonLOSDefaultAllocBytes() {
    return Region.MAX_ALLOC_SIZE;
  }

  // G1 Specific features
  public int g1LogPagesInRegion() { return 8; }
}