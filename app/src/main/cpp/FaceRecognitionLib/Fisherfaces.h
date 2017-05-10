/* Copyright (C) 2016. All rights reserved.

 This software may be distributed and modified under the terms of the GNU
 General Public License version 2 (GPL2) as published by the Free Software
 Foundation and appearing in the file GPL2.TXT included in the packaging of
 this file. Please note that GPL2 Section 2[b] requires that all works based
 on this software must also be made publicly available under the terms of
 the GPL2 ("Copyleft").

modified by brunoc2 and dsgonza2, original: Kristian Sloth Lauszus
*/

#ifndef __fisherfaces_h__
#define __fisherfaces_h__

#include <Eigen/Dense> // http://eigen.tuxfamily.org

#include "Facebase.h"
#include "PCA.h"
#include "LDA.h"

using namespace Eigen;

class Fisherfaces : public Facebase, public LDA {
public:
    /**
     * Train Fisherfaces.
     * @param images  Each images represented as a column vector.
     * @param classes Class labels should start at 1 and increment by 1.
     */
    void train(const MatrixXi &images, const VectorXi &classes);
};

#endif