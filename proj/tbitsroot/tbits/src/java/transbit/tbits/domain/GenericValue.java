package transbit.tbits.domain;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import transbit.tbits.common.DatabaseException;

/**
 * This represents a Generic value, this is specially useful in creating the prepared stetments through various function.
 * While creating the SQL String for preparedStatement, we can't right away set the parameter. So we need an object to store those value.
 * This is where GenericValue comes into play. 
 * 
 * The objects of this class are immutable meaning type and the value can not be altered after the creation of object.
 * 
 * It provides a handy setParametersPS() static function which can be executed over a list of GenericValue and Prepared statement.
 * @author sandeepgiri
 *
 */
public class GenericValue {
	private boolean bitValue = false;
	private Date DateValue;

	private float floatValue = 0;

	private int intValue = 0;

	private String StringValue = null;
	private double DoubleValue;

	private GenericValueType type = null;

	public GenericValue(GenericValueType aType) {
		this.type = aType;
	}

	public GenericValue(double aDoubleValue) {
		this(GenericValueType.DOUBLE);
		this.setDouble(aDoubleValue);
	}
	public GenericValue(int aIntValue) 
	{
		this(GenericValueType.INT);
		this.setInt(aIntValue);
	}

	public GenericValue(float aFloatValue) 
	{
		this(GenericValueType.FLOAT);
		this.setFloat(aFloatValue);
	}
	public GenericValue(boolean aBitValue) 
	{
		this(GenericValueType.BOOLEAN);
		this.setBit(aBitValue);
	}
	
	public GenericValue(Date aDateValue) 
	{
		this(GenericValueType.DATETIME);
		this.setDate(aDateValue);
	}

	public GenericValue(String aStringValue) 
	{
		this(GenericValueType.STRING);
		this.setString(aStringValue);
	}
	
	public Date getDate() {
		return DateValue;
	}

	public float getFloat() {
		return floatValue;
	}

	public int getInt() {
		return intValue;
	}

	public String getString() {
		return StringValue;
	}

	public GenericValueType getType() {
		return type;
	}

	private void setDate(Date dateValue) {
		DateValue = dateValue;
	}

	private void setFloat(float floatValue) {
		this.floatValue = floatValue;
	}

	private void setInt(int intValue) {
		this.intValue = intValue;
	}
	private void setString(String stringValue) {
		StringValue = stringValue;
	}

	private void setBit(boolean bitValue) {
		this.bitValue = bitValue;
	}

	private boolean getBit() {
		return bitValue;
	}

	private void setDouble(double doubleValue) {
		DoubleValue = doubleValue;
	}

	private double getDouble() {
		return DoubleValue;
	}
	
	public static void setParametersPS(PreparedStatement ps,
			ArrayList<GenericValue> params) throws SQLException, DatabaseException {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

		for(int i=0; i < params.size(); i++)
		{
			GenericValue val = params.get(i);
			if(val.getType() == GenericValueType.BOOLEAN)
			{
				ps.setBoolean(i+1, val.getBit());
			}
			else if(val.getType() == GenericValueType.INT)
			{
				ps.setInt(i+1, val.getInt());
			}
			else if(val.getType() == GenericValueType.FLOAT)
			{
				ps.setFloat(i+1, val.getFloat());
			}
			else if(val.getType() == GenericValueType.DOUBLE)
			{
				ps.setDouble(i+1, val.getDouble());
			}

			else if(val.getType() == GenericValueType.STRING)
			{
				ps.setString(i+1, val.getString());
			}
			else if(val.getType() == GenericValueType.DATETIME)
			{
				ps.setTimestamp(i+1, new java.sql.Timestamp(val.getDate().getTime()), cal);
			}
			else
			{
				throw new DatabaseException("Unsupported Data type " + val.getType().toString() + " for Generic Value. ", null);
			}
		}
	}

}

enum GenericValueType {
	BOOLEAN, DATETIME, DOUBLE, FLOAT, INT, LONG, STRING, TIME, UNKNOWN
}
