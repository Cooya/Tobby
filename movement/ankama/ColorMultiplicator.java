package movement.ankama;

import java.util.Hashtable;

public class ColorMultiplicator {
	 public static Hashtable<ColorMultiplicator, Integer> MEMORY_LOG = new Hashtable<ColorMultiplicator, Integer>();
     public double red;
     public double green;
     public double blue;
     private boolean _isOne;
     
     public ColorMultiplicator(int i1, int i2, int i3, boolean bool) {
    	 MEMORY_LOG.put(this, 1);
    	 this.red = i1;
    	 this.green = i2;
    	 this.blue = i3;
    	 if(!bool && (i1 + i2 + i3 == 0))
    		 this._isOne = true;
     }
     
     public static double clamp(double d1, double d2, double d3) {
    	 if(d1 > d3)
    		 return d3;
    	 if(d1 < d2)
    		 return d2;
    	 return d1;
     }
     
     public boolean isOne() {
    	 return this._isOne;
     }
     
     public ColorMultiplicator multiply(ColorMultiplicator cm) {
    	 if(this._isOne)
    		 return cm;
    	 if(cm._isOne)
    		 return this;
    	 ColorMultiplicator cm2 = new ColorMultiplicator(0, 0, 0, false);
    	 cm2.red = this.red + cm.red;
         cm2.green = (this.green + cm.green);
         cm2.blue = (this.blue + cm.blue);
         cm2.red = clamp(cm2.red, -128, 127);
         cm2.green = clamp(cm2.green, -128, 127);
         cm2.blue = clamp(cm2.blue, -128, 127);
         cm2._isOne = false;
         return cm2;
     }
     
     public String toString() {
    	 return "[r: " + this.red + ", g:" + this.green + ", b:" + this.blue + "]";
     }
}
