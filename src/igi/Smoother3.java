package igi;

import edu.mines.jtk.dsp.*;

/**
 * 3D symmetric positive definite smoothing operator 
 * for preconditioning in a CG solver.
 * @author Xinming Wu
 * @version 2018.01.19
 */

public class Smoother3 {

   /**
   * Constructs a 3D smoother.
   * @param sigma smooth half-width.
   * @param wp spatially varying map to stop smoothing near discontinuities.
   * @param et 2d eigentensor field.
   */
  public Smoother3(float sigma, float[][][] wp, EigenTensors3 et) {
    _wp = wp;
    _et = et;
    _sigma = sigma;
    _scale = 0.5f*sigma*sigma;
    _ref = new RecursiveExponentialFilter(sigma);
    _ref.setEdges(_edges);
  }

  /**
   * Smoothing preconditioner for the CG solver.
   * As a preconditioner, the smoothing operator 
   * needs to be symmetric positive definite.
   * @param x input and output after smoothing.
   */
  public void apply(float[][][] x) {
    int n3 = x.length;
    int n2 = x[0].length;
    int n1 = x[0][0].length;
    float[][][] y = new float[n3][n2][n1];
    if (_et==null&&_wp==null) {
      applyRefSmooth(_sigma,x); //isotropic smoothing, very fast
    } else if(_et==null&&_wp!=null) {
      _lsf.applySmoothS(x,y);
      _lsf.apply(_scale,_wp,y,x); //isotropic & spatially variant smoothing
    } else if(_et!=null&&_wp==null) {
      _lsf.applySmoothS(x,y);
      _lsf.apply(_et,_scale,y,x); //anisotropic smoothing
    } else if(_et!=null&&_wp!=null) {
      _lsf.applySmoothS(x,y);
      _lsf.apply(_et,_scale,_wp,y,x); //anisotropic & spatially variant smoothing
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // private
  private float _scale;
  private float _sigma;
  private float[][][] _wp = null;
  private EigenTensors3 _et = null;
  private LocalSmoothingFilter _lsf = new LocalSmoothingFilter();
  private RecursiveExponentialFilter _ref; 
  RecursiveExponentialFilter.Edges _edges =
      RecursiveExponentialFilter.Edges.OUTPUT_ZERO_SLOPE;

  //construct a symmetric positive definite smoothing operator 
  //with highly efficient recursive exponential filters
  private void applyRefSmooth(float sigma, float[][][] x) {
    smooth1(sigma,x);
    smooth2(sigma,x);
    smooth3(sigma,x);
    smooth3(sigma,x);
    smooth2(sigma,x);
    smooth1(sigma,x);
  }

  // Smoothing for dimension 1.
  private void smooth1(float sigma, float[][][] x) {
    _ref.apply1(x,x);
  }
  // Smoothing for dimension 2.
  private void smooth2(float sigma, float[][][] x) {
    _ref.apply2(x,x);
  }
  // Smoothing for dimension 3.
  private void smooth3(float sigma, float[][][] x) {
    _ref.apply3(x,x);
  }

}

