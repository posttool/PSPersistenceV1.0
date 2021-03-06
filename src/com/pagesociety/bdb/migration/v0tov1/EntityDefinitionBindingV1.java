package com.pagesociety.bdb.migration.v0tov1;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.pagesociety.bdb.BDBEntityDefinitionProvider;
import com.pagesociety.bdb.BDBStore;
import com.pagesociety.bdb.binding.FieldBinding;
import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.Types;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;

public class EntityDefinitionBindingV1 extends TupleBinding
{
	private static final Logger logger = Logger.getLogger(EntityDefinitionBindingV1.class);

	public EntityDefinitionBindingV1(){}
	
	
	public void objectToEntry(Object object, TupleOutput to)
	{
		EntityDefinition entity_def = (EntityDefinition) object;
		to.writeString(entity_def.getName());
		List<FieldDefinition> fields = entity_def.getFields();
		to.writeInt(fields.size());
		for (int i = 0; i < fields.size(); i++)
		{
			FieldDefinition f = fields.get(i);
			to.writeString(f.getName());
			to.writeInt(f.getType());
			if(f.getBaseType() == Types.TYPE_REFERENCE)
				write_default_reference_value(f,f.getDefaultValue(),to);
			else
			{
				try{
				FieldBinding.writeValueToTuple(f, f.getDefaultValue(),to);
				}catch(DatabaseException dbe){};/*we can ignore this since it will never happen as we are never saving reference types here */
			}
			if (f.getBaseType() == Types.TYPE_REFERENCE)
				to.writeString(f.getReferenceType());
		
			to.writeBoolean(f.isRequired());
			to.writeBoolean(f.isCascadeOnDelete());
			to.writeString(f.getComment());
		}

	}


	public Object entryToObject(DatabaseEntry data)
	{
		TupleInput ti =  new TupleInput(data.getData(), data.getOffset(),data.getSize());
		EntityDefinition entity_def = new EntityDefinition(ti.readString());
		int size = ti.readInt();
		for (int i = 0; i < size; i++)
		{
			FieldDefinition f = new FieldDefinition(ti.readString(), ti.readInt(),null);
			Object default_value = null;
			if (f.getBaseType() == Types.TYPE_REFERENCE)
				default_value = read_default_reference_value(f,ti);
			else
				default_value = FieldBinding.readValueFromTuple(ti, f);
			
			f.setDefaultValue(default_value);
			if (f.getBaseType() == Types.TYPE_REFERENCE)
				f.setReferenceType(ti.readString());
			
			f.setIsRequired(ti.readBoolean());
			f.setIsCascadeOnDelete(ti.readBoolean());
			f.setComment(ti.readString());
			entity_def.addField(f);
		}

		return entity_def;
	}

	
	/*TODO: we dont eve use this part of the tuple interface */
	public Object entryToObject(TupleInput ti)
	{
		System.err.println("EntityDefBinding: WRONG ENTRY TO OBJECT!!!");
		return null;
	}

	
	//HELPERS//
	
	//the idea with this whole thing....we want to preserve the -1 id of the default
	//value so we now to create a new one on insert. this seems like useful behavior.
	//you can give every user a default collection automagically for instance	
	private void write_default_reference_value(FieldDefinition f,Object value,TupleOutput to)
	{
		if(f.isArray())
			write_default_reference_array_value(value,to);
		else
			write_default_reference_value(value,to);
	}
	
	private void write_default_reference_value(Object val,TupleOutput to)
	{

		Entity e = (Entity)val;
		if(e == null)
		{
			to.writeFast(FieldBinding.NULL_FLAG_VAL_NULL);
			return;
		}
		to.writeFast(FieldBinding.NULL_FLAG_VAL_NOT_NULL);
		to.writeString(e.getType());
		to.writeLong(e.getId());			
	}
	
	private void write_default_reference_array_value(Object val,TupleOutput to)
	{
		List<Entity> ee = (List<Entity>)val;
		if(ee == null)
		{
			to.writeFast(FieldBinding.NULL_FLAG_VAL_NULL);
			return;
		}
		to.writeFast(FieldBinding.NULL_FLAG_VAL_NOT_NULL);
		to.writeInt(ee.size());
		for(int i = 0;i < ee.size();i++)
		{
			write_default_reference_value(ee.get(i), to);
		}
	}
	
	
	//the idea with this whole thing....we want to preserve the -1 id of the default
	//value so we now to create a new one on insert. this seems like useful behavior.
	//you can give every user a default collection automagically for instance	
	private Object read_default_reference_value(FieldDefinition f,TupleInput ti)
	{
		if(f.isArray())
			return read_default_reference_array_value(ti);
		else
			return read_default_reference_value(ti);
	}
	
	private Object read_default_reference_value(TupleInput ti)
	{
		int null_flag = ti.readFast();
		if(null_flag == FieldBinding.NULL_FLAG_VAL_NULL)
			return null;
		else
		{
			Entity e 	= Entity.createInstance();
			String type = ti.readString();
			long id		= ti.readLong();
			e.setType(type);
			e.setId(id);
			return e;
		}
	}
	
	private Object read_default_reference_array_value(TupleInput ti)
	{

		int null_flag = ti.readFast();
		if(null_flag == FieldBinding.NULL_FLAG_VAL_NULL)
			return null;
		else
		{
			int size 		= ti.readInt();
			List<Entity> ee = new ArrayList<Entity>(size);
			for(int i = 0;i<size;i++)
			{
				ee.add((Entity)read_default_reference_value(ti));
			}
			return ee;
		}
	}

}