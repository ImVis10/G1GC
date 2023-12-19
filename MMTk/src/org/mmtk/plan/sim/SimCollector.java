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
import org.mmtk.policy.LargeObjectLocal;
import org.mmtk.policy.MarkSweepLocal;
import org.mmtk.utility.alloc.Allocator;
import org.mmtk.vm.VM;
import org.mmtk.policy.CopyLocal;
import org.mmtk.policy.CopySpace;
import org.mmtk.policy.MarkSweepSpace;
import org.vmmagic.pragma.*;
import org.vmmagic.unboxed.*;

/**
 * This class implements <i>per-collector thread</i> behavior
 * and state for the <i>CopyMS</i> plan.<p>
 *
 * Specifically, this class defines <i>CopyMS</i>
 * collection behavior (through <code>trace</code> and
 * the <code>collectionPhase</code> method), and
 * collection-time allocation into the mature space.
 *
 * @see Sim
 * @see SimMutator
 * @see StopTheWorldCollector
 * @see CollectorContext
 */
@Uninterruptible
public class SimCollector extends StopTheWorldCollector {

  /****************************************************************************
   * Instance fields
   */

  /**
   *
   */
    private final CopyLocal youngSpace;
    private final MarkSweepLocal survivorSpace;
    private final MarkSweepLocal oldSpace;
//    private final CopyMSTraceLocal youngTrace;
//    private final CopyMSTraceLocal survivorTrace;
//    private final CopyMSTraceLocal oldTrace;

    private final SimTraceLocal trace;

    protected final LargeObjectLocal los;

  /****************************************************************************
   *
   * Initialization
   */

  /**
   * Create a new (local) instance.
   */
  public SimCollector() {
      los = new LargeObjectLocal(Plan.loSpace);
      youngSpace = new CopyLocal(Sim.youngSpace);
      survivorSpace = new MarkSweepLocal(Sim.survivorSpace);
      oldSpace = new MarkSweepLocal(Sim.oldSpace);
//      youngTrace = new CopyMSTraceLocal(global().youngTrace);
//      survivorTrace = new CopyMSTraceLocal(global().survivorTrace);
//      oldTrace = new CopyMSTraceLocal(global().oldTrace);
      trace = new SimTraceLocal(global().trace);

 }

  /****************************************************************************
   *
   * Collection-time allocation
   */

  /**
   * {@inheritDoc}
   */
  @Override
  @Inline
  public final Address allocCopy(ObjectReference original, int bytes,
      int align, int offset, int allocator) {
//    if (allocator == Plan.ALLOC_LOS) {
//      if (VM.VERIFY_ASSERTIONS) VM.assertions._assert(Allocator.getMaximumAlignedSize(bytes, align) > Plan.MAX_NON_LOS_COPY_BYTES);
//      return los.alloc(bytes, align, offset);
//    } else {
//      if (VM.VERIFY_ASSERTIONS) {
//        VM.assertions._assert(bytes <= Plan.MAX_NON_LOS_COPY_BYTES);
//        VM.assertions._assert(allocator == CopyMS.ALLOC_MS);
//      }
//      return mature.alloc(bytes, align, offset);
//    }
      if (allocator == Sim.ALLOC_YOUNG) {
          return youngSpace.alloc(bytes, align, offset);
      } else if (allocator == Sim.ALLOC_SURVIVOR) {
          return survivorSpace.alloc(bytes, align, offset);
      } else if (allocator == Sim.ALLOC_OLD) {
          if (VM.VERIFY_ASSERTIONS) {
              VM.assertions._assert(bytes <= Plan.MAX_NON_LOS_COPY_BYTES);
          }
          return oldSpace.alloc(bytes, align, offset);
      } else if (allocator == Plan.ALLOC_LOS) {
          if (VM.VERIFY_ASSERTIONS) VM.assertions._assert(Allocator.getMaximumAlignedSize(bytes, align) > Plan.MAX_NON_LOS_COPY_BYTES);
          return los.alloc(bytes, align, offset);
      } else {
          // Default to the old space
          if (VM.VERIFY_ASSERTIONS) {
              VM.assertions._assert(bytes <= Plan.MAX_NON_LOS_COPY_BYTES);
          }
          return oldSpace.alloc(bytes, align, offset);
      }
  }

  @Override
  @Inline
  public final void postCopy(ObjectReference object, ObjectReference typeRef,
      int bytes, int allocator) {
//    if (allocator == Plan.ALLOC_LOS)
//      Plan.loSpace.initializeHeader(object, false);
//    else
//      CopyMS.msSpace.postCopy(object, true);
      if (allocator == Sim.ALLOC_YOUNG) {
          // Young space does not require post-copy actions
      } else if (allocator == Sim.ALLOC_SURVIVOR) {
          // Survivor space does not require post-copy actions
      } else if (allocator == Sim.ALLOC_OLD) {
          Sim.oldSpace.postCopy(object, true);
      } else if (allocator == Plan.ALLOC_LOS) {
          Plan.loSpace.initializeHeader(object, false);
      } else {
          // Default to the old space
          Sim.oldSpace.postCopy(object, true);
      }
  }

  /****************************************************************************
   *
   * Collection
   */

  /**
   * {@inheritDoc}
   */
  @Override
  @Inline
  public final void collectionPhase(short phaseId, boolean primary) {
//    if (phaseId == CopyMS.PREPARE) {
//      super.collectionPhase(phaseId, primary);
//      mature.prepare();
//      trace.prepare();
//      return;
//    }
//
//    if (phaseId == CopyMS.CLOSURE) {
//      trace.completeTrace();
//      return;
//    }
//
//    if (phaseId == CopyMS.RELEASE) {
//      mature.release();
//      trace.release();
//      super.collectionPhase(phaseId, primary);
//      return;
//    }
//
//    super.collectionPhase(phaseId, primary);
      if (phaseId == Sim.PREPARE) {
          super.collectionPhase(phaseId, primary);
          survivorSpace.prepare();
          oldSpace.prepare();
          trace.prepare();
          return;
      }

      if (phaseId == Sim.CLOSURE) {
          trace.completeTrace();
          return;
      }

      if (phaseId == Sim.RELEASE) {
    	  survivorSpace.release();
    	  oldSpace.release();
          trace.release();
          super.collectionPhase(phaseId, primary);
          return;
      }

//      super.collectionPhase(phaseId, primary);
  }

  /****************************************************************************
   *
   * Miscellaneous
   */

  /** @return the active global plan as an <code>MS</code> instance. */
  @Inline
  private static Sim global() {
    return (Sim) VM.activePlan.global();
//	  return CopyMS.youngSpace
  }

  /** @return The current trace instance. */
  @Override
  public final TraceLocal getCurrentTrace() {
    return trace;      
  }

}