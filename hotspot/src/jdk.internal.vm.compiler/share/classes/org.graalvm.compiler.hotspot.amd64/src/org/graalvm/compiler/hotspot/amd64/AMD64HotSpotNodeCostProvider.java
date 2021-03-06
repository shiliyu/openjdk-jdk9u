/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.graalvm.compiler.hotspot.amd64;

import static org.graalvm.compiler.nodeinfo.NodeCycles.CYCLES_15;
import static org.graalvm.compiler.nodeinfo.NodeCycles.CYCLES_50;
import static org.graalvm.compiler.nodeinfo.NodeCycles.CYCLES_6;
import static org.graalvm.compiler.nodeinfo.NodeCycles.CYCLES_80;
import static org.graalvm.compiler.nodeinfo.NodeSize.SIZE_30;
import static org.graalvm.compiler.nodeinfo.NodeSize.SIZE_4;

import org.graalvm.compiler.graph.Node;
import org.graalvm.compiler.hotspot.nodes.HotSpotNodeCostProvider;
import org.graalvm.compiler.nodeinfo.NodeCycles;
import org.graalvm.compiler.nodeinfo.NodeSize;
import org.graalvm.compiler.nodes.ReturnNode;
import org.graalvm.compiler.replacements.nodes.ArrayEqualsNode;
import org.graalvm.compiler.replacements.nodes.UnaryMathIntrinsicNode;

import jdk.vm.ci.amd64.AMD64;
import jdk.vm.ci.amd64.AMD64.CPUFeature;
import jdk.vm.ci.code.TargetDescription;

public class AMD64HotSpotNodeCostProvider extends HotSpotNodeCostProvider {
    private final boolean avx2;
    private final boolean sse41;

    public AMD64HotSpotNodeCostProvider(TargetDescription target) {
        this.avx2 = ((AMD64) target.arch).getFeatures().contains(CPUFeature.AVX2);
        this.sse41 = ((AMD64) target.arch).getFeatures().contains(CPUFeature.SSE4_1);
    }

    @Override
    public NodeCycles cycles(Node n) {
        if (n instanceof UnaryMathIntrinsicNode) {
            UnaryMathIntrinsicNode u = (UnaryMathIntrinsicNode) n;
            switch (u.getOperation()) {
                case LOG:
                case LOG10:
                    return CYCLES_15;
                default:
                    break;
            }
        } else if (n instanceof ReturnNode) {
            return CYCLES_6;
        } else if (n instanceof ArrayEqualsNode) {
            if (avx2) {
                return CYCLES_50;
            } else if (sse41) {
                return CYCLES_80;
            }
        }
        return super.cycles(n);
    }

    @Override
    public NodeSize size(Node n) {
        if (n instanceof UnaryMathIntrinsicNode) {
            UnaryMathIntrinsicNode u = (UnaryMathIntrinsicNode) n;
            switch (u.getOperation()) {
                case LOG:
                case LOG10:
                    return SIZE_30;
                default:
                    break;
            }
        } else if (n instanceof ReturnNode) {
            return SIZE_4;
        }
        return super.size(n);
    }

}
