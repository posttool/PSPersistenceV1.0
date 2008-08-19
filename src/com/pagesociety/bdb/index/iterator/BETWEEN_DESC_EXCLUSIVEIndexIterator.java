package com.pagesociety.bdb.index.iterator;

import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;

public class BETWEEN_DESC_EXCLUSIVEIndexIterator extends RangeIndexIterator
{
	DatabaseEntry original_search_key;
	/*ARG 1 IS TOP ARG 2 IS BOTTOM i.e. between desc 35 12*/
	public void open(IterableIndex index,Object... user_args) throws DatabaseException
	{
		super.open(index,user_args);
		last_opstat	=	index_cursor.getSearchKeyRange(key, data, LockMode.DEFAULT);
		if(last_opstat == OperationStatus.SUCCESS)
			last_opstat = index_cursor.getPrev(key, data, LockMode.DEFAULT);
		else
			last_opstat = index_cursor.getLast(key, data, LockMode.DEFAULT);

		check_terminal_key();
	}
	
	//TODO: need to make sure that we dont restore to a terminal condition 
	//in the event that getSearchBothFails
	public void resume(IterableIndex index,Object token) throws DatabaseException
	{
		super.resume(index,token);
		last_opstat	=	index_cursor.getSearchBoth(key, data, LockMode.DEFAULT);
		if(last_opstat == OperationStatus.NOTFOUND)
		{
			last_opstat	=	index_cursor.getSearchBothRange(key, data, LockMode.DEFAULT);
			if(last_opstat == OperationStatus.SUCCESS)
				last_opstat =   index_cursor.getPrev(key, data, LockMode.DEFAULT);
			else
				index_cursor.getLast(key, data, LockMode.DEFAULT);
		}
	}
	
	public DatabaseEntry currentKey()
	{
		return key;
	}
	
	public DatabaseEntry currentData() 
	{	
		return data;
	}

	public boolean isValid() 
	{
		return (last_opstat == OperationStatus.SUCCESS);
	}
	
	public boolean isDone() 
	{
		return (last_opstat == OperationStatus.NOTFOUND);
	}
	
	public void next() throws DatabaseException
	{
		last_opstat =  index_cursor.getPrevDup(key, data, LockMode.DEFAULT);	
		if(last_opstat == OperationStatus.NOTFOUND)
		{
			last_opstat = index_cursor.getPrevNoDup(key, data, LockMode.DEFAULT);
			check_terminal_key();
		}
	}
	
	private void check_terminal_key()
	{
		if(last_opstat == OperationStatus.SUCCESS)
		{
			int l1 = key.getSize();
			int l2 = terminal_key_length;
			//int l = (l1 < l2)?l1:l2;
			if(IteratorUtil.compareDatabaseEntries(key, 0, l1, terminal_key, 0, l2) <= 0)
				last_opstat =  OperationStatus.NOTFOUND;
		}
	}
}