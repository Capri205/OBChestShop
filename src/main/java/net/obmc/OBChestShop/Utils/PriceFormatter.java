package net.obmc.OBChestShop.Utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;

public class PriceFormatter {

	private Double value;
	private Optional<Integer> precision = Optional.of(2);
	private Optional<Boolean> docomma = Optional.of(false);
	private String formatstring = setFormatString();
	
	public PriceFormatter(Double value, Optional<Integer> precision, Optional<Boolean> docomma) {
		this.value = value;
		this.precision = precision;
		this.docomma = docomma;
	}
	
	public void setPrecision(int precision) {
		this.precision = Optional.of(precision);
	}
	
	public String toString() {
		return new DecimalFormat(formatstring, DecimalFormatSymbols.getInstance(Locale.US)).format(value);
	}
	
	private String setFormatString() {
		formatstring = "#.";
		for(int i = 0; i < precision.get(); i++) {
			formatstring += "0";
		}
		formatstring += "#";
		return formatstring;
	}
}
