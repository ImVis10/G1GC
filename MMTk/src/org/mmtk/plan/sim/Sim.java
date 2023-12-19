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
package org.mmtk.plan.sim;

import org.mmtk.plan.*;
import org.mmtk.policy.CopySpace;
import org.mmtk.policy.MarkSweepSpace;
import org.mmtk.policy.Space;
import org.mmtk.utility.heap.VMRequest;
import org.mmtk.utility.options.Options;
import org.mmtk.utility.sanitychecker.SanityChecker;

import org.vmmagic.pragma.*;
import org.vmmagic.unboxed.ObjectReference;

/**
 * This class implements the global state of a full-heap collector
 * with a copying nursery and mark-sweep mature space.  Unlike a full
 * generational collector, there is no write barrier, no remembered set, and
 * every collection is full-heap.<p>
 *
 * All plans make a clear distinction between <i>global</i> and
 * <i>thread-local</i> activities, and divides global and local state
 * into separate class hierarchies.  Global activities must be
 * synchronized, whereas no synchronization is required for
 * thread-local activities.  There is a single instance of Plan (or the
 * appropriate sub-class), and a 1:1 mapping of PlanLocal to "kernel
 * threads" (aka CPUs).  Thus instance
 * methods of PlanLocal allow fast, unsychronized access to functions such as
 * allocation and collection.<p>
 *
 * The global instance defines and manages static resources
 * (such as memory and virtual memory resources).  This mapping of threads to
 * instances is crucial to understanding the correctness and
 * performance properties of MMTk plans.
 */
@Uninterruptible
public class Sim extends StopTheWorld {

  /****************************************************************************
   * Constants
   */

  /****************************************************************************
   * Class variables
   */

  /**
   *
   */

  public static final int NURSERY = 0;
  public static final int SURVIVOR = 1;
  public static final int OLD = 2;

  public static final CopySpace youngSpace = new CopySpace("young", true, VMRequest.highFraction(0.15f));
  public static final MarkSweepSpace survivorSpace = new MarkSweepSpace("survivor", VMRequest.discontiguous());
  public static final MarkSweepSpace oldSpace = new MarkSweepSpace("old", VMRequest.discontiguous());

  public static final int ALLOC_YOUNG = ALLOC_DEFAULT;
  public static final int ALLOC_SURVIVOR = ALLOC_DEFAULT + 1;
  public static final int ALLOC_OLD = ALLOC_DEFAULT + 2;

//  public static final int SCAN_COPYMS = 0;
//
//  // Instance variables for each space
//  public final Trace youngTrace;
//  public final Trace survivorTrace;
//  public final Trace oldTrace;


//  public static final CopySpace nurserySpace = new CopySpace("nursery", false, VMRequest.highFraction(0.15f));
//  public static final MarkSweepSpace msSpace = new MarkSweepSpace("ms", VMRequest.discontiguous());
//
//  public static final int NURSERY = nurserySpace.getDescriptor();
//  public static final int MARK_SWEEP = msSpace.getDescriptor();
//
//  public static final int ALLOC_NURSERY = ALLOC_DEFAULT;
//  public static final int ALLOC_MS = StopTheWorld.ALLOCATORS + 1;
//
  public static final int SCAN_COPYMS = 0;


//  public final Trace youngTrace;
//  public final Trace survivorTrace;
//  public final Trace oldTrace;
  /****************************************************************************
   * Instance variables
   */

  /**
   *
   */
  public final Trace trace;

  /**
   * Constructor.
 */
//  public CopyMS() {
//    trace = new Trace(metaDataSpace);
//  }
  public Sim() {
	  trace  = new Trace(metaDataSpace);
  }

  /*****************************************************************************
   * Collection
   */

  /**
   * {@inheritDoc}
   */
  @Override
  @Inline
  public final void collectionPhase(short phaseId) {
//    if (phaseId == PREPARE) {
//      super.collectionPhase(phaseId);
//      trace.prepare();
//      msSpace.prepare(true);
//      nurserySpace.prepare(true);
//      return;
//    }
//    if (phaseId == CLOSURE) {
//      trace.prepare();
//      return;
//    }
//    if (phaseId == RELEASE) {
//      trace.release();
//      msSpace.release();
//      nurserySpace.release();
//      switchNurseryZeroingApproach(nurserySpace);
//      super.collectionPhase(phaseId);
//      return;
//    }
//
//    super.collectionPhase(phaseId);

    if (phaseId == PREPARE) {
      super.collectionPhase(phaseId);
      trace.prepare();
      survivorSpace.prepare(true);
      oldSpace.prepare(true);
      return;
    }
    if (phaseId == CLOSURE) {
    	trace.prepare();
      return;
    }
    if (phaseId == RELEASE) {
    	trace.release();
      survivorSpace.release();
      oldSpace.release();
      switchNurseryZeroingApproach(youngSpace);
      super.collectionPhase(phaseId);
      return;
    }
    super.collectionPhase(phaseId);
  }

  @Override
  public final boolean collectionRequired(boolean spaceFull, Space space) {
	  //boolean young = nurserySpace.reservedPages() > Options.nurserySize.getMaxNursery();
//
//    return super.collectionRequired(spaceFull, space) || nurseryFull;
    boolean youngFull = youngSpace.reservedPages() > Options.nurserySize.getMaxNursery();
    boolean survivorFull = survivorSpace.reservedPages() > Options.nurserySize.getMaxNursery();
    boolean oldFull = oldSpace.reservedPages() > Options.nurserySize.getMaxNursery();

    return super.collectionRequired(spaceFull, space) || youngFull || survivorFull;

  }

  /*****************************************************************************
   *
   * Accounting
   */

  /**
   * {@inheritDoc}
   */
  @Override
  public int getPagesUsed() {
//    return super.getPagesUsed() +
//      msSpace.reservedPages() +
//      nurserySpace.reservedPages();
    return super.getPagesUsed() +
            oldSpace.reservedPages() +
            youngSpace.reservedPages() +
            survivorSpace.reservedPages();
  }

  /**
   * Return the number of pages reserved for collection.
   * For mark sweep this is a fixed fraction of total pages.
   */
  @Override
  public int getCollectionReserve() {
//    return nurserySpace.reservedPages() + super.getCollectionReserve();
    return youngSpace.reservedPages() + survivorSpace.reservedPages() + oldSpace.reservedPages() + super.getCollectionReserve();

  }

  /**
   * @return The number of pages available for allocation, <i>assuming
   * all future allocation is to the nursery</i>.
   */
  @Override
  public final int getPagesAvail() {
    return (getTotalPages() - getPagesReserved()) >> 1;
  }

  @Override
  public int sanityExpectedRC(ObjectReference object, int sanityRootRC) {
    Space space = Space.getSpaceForObject(object);

    // Nursery
//    if (space == CopyMS.nurserySpace) {
//      return SanityChecker.DEAD;
//    }
//
//    return space.isReachable(object) ? SanityChecker.ALIVE : SanityChecker.DEAD;
    if (space == youngSpace) {
      return SanityChecker.DEAD;
    }

    // Other spaces
    return space.isReachable(object) ? SanityChecker.ALIVE : SanityChecker.DEAD;
  }

  @Override
  @Interruptible
  protected void registerSpecializedMethods() {
//    TransitiveClosure.registerSpecializedScan(SCAN_COPYMS, CopyMSTraceLocal.class);
//    super.registerSpecializedMethods();
    TransitiveClosure.registerSpecializedScan(SCAN_COPYMS, SimTraceLocal.class);
    super.registerSpecializedMethods();
  }

  @Interruptible
  @Override
  public void fullyBooted() {
//    super.fullyBooted();
//    nurserySpace.setZeroingApproach(Options.nurseryZeroing.getNonTemporal(), Options.nurseryZeroing.getConcurrent());
    super.fullyBooted();
    youngSpace.setZeroingApproach(Options.nurseryZeroing.getNonTemporal(), Options.nurseryZeroing.getConcurrent());

  }
}