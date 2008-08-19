package com.pagesociety.persistence;

/**
 * Defines a field including its name, type and other attributes. Field
 * definition are used by entity definitions as well as entity indexes.
 *
 *
 * @author Topher LaFata
 * @author David Karam
 *
 * @see EntityDefinition
 * @see EntityIndex
 */
public class FieldDefinition implements java.io.Serializable
{
	/* used if you want a reference field to be an untyped entity*/
	/* fillReference methods will key off this value to deal with */
	/* singleton and list untyped entity references */
	public static final String REF_TYPE_UNTYPED_ENTITY = "*";
	
	// !!!!!! REMEMBER TO UPDATE CLONE METHOD IF YOU ADD OR REMOVE FIELDS !!!!!!!//
	private String 	_name;
	private int 	_type;
	private String 	_ref_type;

	//private int _array_dimensionality;
	private boolean _required;
	private String 	_description;

	/**
	 * A default constructor. Usage requires that the name and type be set
	 * before the programmer considers the field definition valid.
	 */
	public FieldDefinition()
	{
	}

	/**
	 * A constructor for non reference typed fields.
	 *
	 * @param name
	 *            The name of the field.
	 * @param type
	 *            The type of the field.
	 * @see Types
	 */
	public FieldDefinition(String name, int type)
	{
		if ((type & ~Types.TYPE_ARRAY) == Types.TYPE_REFERENCE)
			throw new RuntimeException("Field " + name + " must be defined with a reference type.");
		_name = name;
		_type = type;
		_ref_type = null;
		//_array_dimensionality = 0;
	}

	/**
	 * A constructor for field that are TYPE_REFERENCE.
	 *
	 * @param name
	 *            The name of the field.
	 * @param type
	 *            The type of the field.
	 * @param ref_type
	 *            The name of the entity definition to which this field refers.
	 */
	public FieldDefinition(String name, int type, String ref_type)
	{
		_name = name;
		_type = type;
		_ref_type = ref_type;
		//_array_dimensionality = 0;
	}

	/**
	 * Set the name of the field.
	 *
	 * @param name
	 *            The name of the field.
	 */
	public void setName(String name)
	{
		_name = name;
	}

	/**
	 * Return the name of the field.
	 *
	 * @return The name of the field.
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * Return the type. This type might include an array flag. Use getBaseType
	 * and isArray to get the exploded typing information.
	 *
	 * @return The type.
	 * @see #getBaseType()
	 * @see #isArray()
	 */
	public int getType()
	{
		return _type;
	}

	/**
	 * Sets the type of this field.
	 *
	 * @param type
	 *            The type
	 * @see Types
	 */
	public void setType(int type)
	{
		_type = type;
	}

	/**
	 * Gets the description for this field.
	 *
	 * @return The description.
	 */
	public String getDescription()
	{
		return _description;
	}

	/**
	 * Sets the description for this field.
	 *
	 * @param description
	 *            The description
	 */
	public void setDescription(String description)
	{
		this._description = description;
	}

	/**
	 * Sets whether this field is required or not.
	 *
	 * @param b
	 *            Is required
	 */
	public void setRequired(boolean b)
	{
		this._required = b;
	}

	/**
	 * Returns whether this field is required.
	 *
	 * @return true if the field is required.
	 */
	public boolean getRequired()
	{
		return _required;
	}

	/**
	 * Returns the 'base' type of this field. The base type masks the array flag
	 * from the type.
	 *
	 * @return The base type.
	 */
	public int getBaseType()
	{
		return _type & ~Types.TYPE_ARRAY;
	}

	/**
	 * Returns whether the type information contains the array flag.
	 *
	 * @return true if this field is an array.
	 */
	public boolean isArray()
	{
		return ((_type & Types.TYPE_ARRAY) == Types.TYPE_ARRAY);
	}

	/**
	 * Set this field to be an array.
	 *
	 * @param b
	 *            true if this field should be an array.
	 */
	public void setIsArray(boolean b)
	{
		if (b)
		{
			_type = _type | Types.TYPE_ARRAY;
			// default to 1
			//_array_dimensionality = 1;
		}
		else
		{
			_type = _type & ~Types.TYPE_ARRAY;
			//_array_dimensionality = 0;
		}
	}

	/**
	 * Set the reference type. The reference type is specified by the name of an
	 * entity definition. All references are to other entity definitions.
	 *
	 * @param ref_type
	 *            The name of the entity definition to reference.
	 * @see Types#TYPE_REFERENCE
	 */
	public void setReferenceType(String ref_type)
	{
		_ref_type = ref_type;
	}

	/**
	 * Returns the reference type or null if the field does not have a reference
	 * base type.
	 *
	 * @return The reference type or null.
	 */
	public String getReferenceType()
	{
		return _ref_type;
	}

//	/**
//	 * Gets the array dimensionality. Currently only 1 dimensional arrays are
//	 * supported.
//	 *
//	 * @return The array dimensionality (1).
//	 */
//	public int getArrayDimensionality()
//	{
//		return _array_dimensionality;
//	}

//	/**
//	 * Sets the array dimensionality. Currently only 1 dimensional arrays are
//	 * supported.
//	 *
//	 * @param d
//	 */
//	public void setArrayDimensionality(int d)
//	{
//		_array_dimensionality = d;
//	}

	/**
	 * Creates a clone of the field definition, copying all fields.
	 */
	public FieldDefinition clone()
	{
		FieldDefinition f = new FieldDefinition();
		f._name = _name;
		f._type = _type;
		f._ref_type = _ref_type;
		//f._array_dimensionality = _array_dimensionality;
		f._description = _description;
		f._required = _required;
		return f;
	}

	/**
	 * Compares all properties of one field definition to another.
	 */
	public boolean equals(Object o)
	{
		if (!(o instanceof FieldDefinition))
			return false;
		FieldDefinition f = (FieldDefinition) o;
		if (!_name.equals(f._name))
			return false;
		if (_type != f._type)
			return false;
		if (_ref_type == null && f._ref_type != null)
			return false;
		if (_ref_type != null && !_ref_type.equals(f._ref_type))
			return false;
		//if (_array_dimensionality != f._array_dimensionality)
		//	return false;
		if (_required != f._required)
			return false;
		return true;
	}

	/**
	 * Returns a text representation of this object.
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("Field: " + getName() + " is ");
		int type = getType();
		if ((type & Types.TYPE_ARRAY) == Types.TYPE_ARRAY)
		{
			type = getBaseType();
			buf.append("an array of ");
			buf.append(toStringBaseType(type));
			return buf.toString();
		}
		else
		{
			String t = toStringBaseType(type);
			buf.append("a");
			if (t.startsWith("I"))
				buf.append("n");
			buf.append(" ");
			buf.append(t);
		}
		return buf.toString();
	}

	/**
	 * A utility function for decoding type directly from encoded type int.
	 *
	 * @param type
	 * @return A text representation of a type.
	 */
	public static String typeAsString(int type)
	{
		if ((type & Types.TYPE_ARRAY) == Types.TYPE_ARRAY)
		{
			StringBuffer buf = new StringBuffer();
			buf.append("Array of ");
			buf.append(toStringBaseType(type));
			return buf.toString();
		}
		else
		{
			return toStringBaseType(type);
		}
	}

	private static String toStringBaseType(int type)
	{
		switch (type)
		{
		case Types.TYPE_UNDEFINED:
			return "Undefined";
		case Types.TYPE_BOOLEAN:
			return "Boolean";
		case Types.TYPE_LONG:
			return "Long";
		case Types.TYPE_INT:
			return "Integer";
		case Types.TYPE_DOUBLE:
			return "Double";
		case Types.TYPE_FLOAT:
			return "Float";
		case Types.TYPE_STRING:
			return "String";
		case Types.TYPE_TEXT:
			return "Text";
		case Types.TYPE_DATE:
			return "Date";
		case Types.TYPE_BLOB:
			return "Date";
		case Types.TYPE_REFERENCE:
			return "Reference";
		default:
			return "Unknown Type(" + type + ")";
		}
	}
}