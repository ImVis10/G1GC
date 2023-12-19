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

import org.mmtk.plan.StopTheWorldMutator;
import org.mmtk.policy.CopyLocal;
import org.mmtk.policy.MarkSweepLocal;
import org.mmtk.policy.Space;

import org.mmtk.utility.alloc.Allocator;

import org.vmmagic.pragma.*;
import org.vmmagic.unboxed.*;

/**
 * This class implements <i>per-mutator thread</i> behavior
 * and state for the <i>CopyMS</i> plan.<p>
 *
 * Specifically, this class defines <i>CopyMS</i> mutator-time
 * allocation into the nursery and mature space (through pre-tenuring).
 * Per-mutator thread collection semantics are also defined (flushing
 * and restoring per-mutator allocator state).
 *
 * @see Sim
 * @see SimCollector
 * @see org.mmtk.plan.StopTheWorldMutator
 * @see org.mmtk.plan.MutatorContext
 */
@Uninterruptible
public class SimMutator extends StopTheWorldMutator {

  /****************************************************************************
   * Instance fields
   */

  /**
   *
   */
//  private final MarkSweepLocal mature;
//  private final CopyLocal nursery;

  private final CopyLocal nursery;
  private final MarkSweepLocal survivor;
  private final MarkSweepLocal old;

  /****************************************************************************
   *
   * Initialization
   */

  /**
   * Constructor
   */
//  public CopyMSMutator() {
//    mature = new MarkSweepLocal(CopyMS.msSpace);
//    nursery = new CopyLocal(CopyMS.nurserySpace);

//  }

  public SimMutator() {
    nursery = new CopyLocal(Sim.youngSpace);
    survivor = new MarkSweepLocal(Sim.survivorSpace);
    old = new MarkSweepLocal(Sim.oldSpace);
  }
  /****************************************************************************
   *
   * Mutator-time allocation
   */

  /**
   * {@inheritDoc}<p>
   *
   * This class handles the default allocator from the mark sweep space,
   * and delegates everything else to the superclass.
   */
  @Override
  @Inline
  public Address alloc(int bytes, int align, int offset, int allocator, int site) {
//    if (allocator == CopyMS.ALLOC_DEFAULT)
//      return nursery.alloc(bytes, align, offset);
//    if (allocator == CopyMS.ALLOC_MS)
//      return mature.alloc(bytes, align, offset);
//
//    return super.alloc(bytes, align, offset, allocator, site);
    if (allocator == Sim.ALLOC_YOUNG)
      return nursery.alloc(bytes, align, offset);
    else if (allocator == Sim.ALLOC_SURVIVOR)
      return survivor.alloc(bytes, align, offset);
    else if (allocator == Sim.ALLOC_OLD)
      return old.alloc(bytes, align, offset);
    else
      return super.alloc(bytes, align, offset, allocator, site);
  }

  /**
   * {@inheritDoc}<p>
   *
   * Initialize the object header for objects in the mark-sweep space,
   * and delegate to the superclass for other objects.
   */
  @Override
  @SuppressWarnings({"UnnecessaryReturnStatement"})
  @Inline
  public void postAlloc(ObjectReference ref, ObjectReference typeRef,
      int bytes, int allocator) {
//    if (allocator == CopyMS.ALLOC_DEFAULT)
//      return;
//    else if (allocator == CopyMS.ALLOC_MS)
//      CopyMS.msSpace.initializeHeader(ref, true);
//    else
//      super.postAlloc(ref, typeRef, bytes, allocator);
    if (allocator == Sim.ALLOC_YOUNG)
      return;
    else if (allocator == Sim.ALLOC_SURVIVOR)
      Sim.survivorSpace.initializeHeader(ref, true);
    else if (allocator == Sim.ALLOC_OLD)
      Sim.oldSpace.initializeHeader(ref, true);
    else
      super.postAlloc(ref, typeRef, bytes, allocator);
  }

  @Override
  public Allocator getAllocatorFromSpace(Space space) {
//    if (space == CopyMS.nurserySpace) return nursery;
//    if (space == CopyMS.msSpace) return mature;
//    return super.getAllocatorFromSpace(space);
    if (space == Sim.youngSpace) return nursery;
    else if (space == Sim.survivorSpace) return survivor;
    else if (space == Sim.oldSpace) return old;
    return super.getAllocatorFromSpace(space);
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
//      return;
//    }
//
//    if (phaseId == CopyMS.RELEASE) {
//      nursery.reset();
//      mature.release();
//      super.collectionPhase(phaseId, primary);
//      return;
//    }
//
//    super.collectionPhase(phaseId, primary);
    if (phaseId == Sim.PREPARE) {
      super.collectionPhase(phaseId, primary);
//      nursery.prepare();
      survivor.prepare();
      old.prepare();
      return;
    }

    if (phaseId == Sim.RELEASE) {
      nursery.reset();
      survivor.release();
      old.release();
      super.collectionPhase(phaseId, primary);
      return;
    }

    super.collectionPhase(phaseId, primary);
  }

  @Override
  public void flush() {
//    super.flush();
//    mature.flush();
    super.flush();
//    nursery.flush();
    survivor.flush();
    old.flush();
  }
}