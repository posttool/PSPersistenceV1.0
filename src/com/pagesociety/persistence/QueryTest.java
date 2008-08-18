package com.pagesociety.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;

import com.pagesociety.bdb.BDBStore;
import com.pagesociety.bdb.BDBStoreConfigKeyValues;

public class QueryTest {

	/**
	 * @param args
	 */

	public static void main(String[] args) 
	{
		 BasicConfigurator.configure();
		new QueryTest();
	}
	
	private BDBStore _store;
	long t1;
	long t2;
	
	public QueryTest()
	{
		try{
			init_store();
			insert_entity_definitions();
			run_test();
			for(Thread t:ACTIVE_THREADS)
			{
				try{
					t.join();
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			_store.close();

		}catch(PersistenceException pe)
		{
			pe.printStackTrace();
		//	try{
		//		_store.close();
		//	}catch(Exception e)
		//	{
		//		e.printStackTrace();
		//		System.out.println("FAILED TO CLOSE DB");
		//	}
		}
		finally{
		
		}
	}
	public void run_test() throws PersistenceException
	{
		//shutdown_test();
		//set_contains_all_test();
		//forensic_test();
		//pkey_query_test();
		//order_by_test();
		//double_query_test();
		//delete_test();
		//string_min_test();
		//string_min_test();
		//count_test();
		//single_predicate_test();
		//single_paging_predicate_test();
		//multi_predicate_test_reference();
		
		//array_index_test();
		//for(int i = 0; i < 10;i++)
			//multi_array_index_test();
		//range_test();
		//intersect_test();
		//union_test();
		pssql_test();
		//concurrency_test();
	}
	
	private List<Thread> ACTIVE_THREADS = new ArrayList<Thread>();
	public void concurrency_test() throws PersistenceException
	{
		insert_entity_instances(100);
		addSingleFieldEntityIndex("Author", "FirstName", EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX, "byFirstName", null);
		for(int i = 0;i < 25;i++)
		{
			double d = Math.random();
			if(d >0.5)
			{
				R reader = new R();
				ACTIVE_THREADS.add(reader);
				reader.start();
			

			}
			else
			{
				U updater = new U();
				ACTIVE_THREADS.add(updater);
				updater.start();		

			}
		}
	}
	
	class R extends Thread
	{
		public void run() 
		{
			System.out.println(getId()+" STARTING READER ");
			try{
				Query q = new Query("Author");
				q.idx("byFirstName");
				String fn = R(firstNames);
				q.eq(fn);
				q.cacheResults(false);
				QueryResult result = _store.executeQuery(q);
				System.out.println(getId()+" GOT RESULTS "+result.size());
			}catch(Exception e)
			{
				System.err.println("R");
				e.printStackTrace();
			}
		}
	}
	int NNN = 100;
	class U extends Thread
	{
		public void run() 
		{
			System.out.println(getId()+" STARTING WRITER ");
			try{
				
				Query q = new Query("Author");
				q.idx(Query.PRIMARY_IDX);
				q.eq(Query.VAL_GLOB);
				q.cacheResults(false);
				QueryResult result = _store.executeQuery(q);
				List<Entity> ee = result.getEntities();
				
				for(int i = 0;i < ee.size();i++)
				{
					Entity a = ee.get(i);
					String fn = R(firstNames);
					a.setAttribute("FirstName",fn);
					//System.out.println(getId()+" SETTING FIRSTNAME TO "+fn);
					_store.saveEntity(a);
			
					insert_entity_instances(1);
					//try{Thread.sleep(250);}catch(Exception e){}
				}
			
				//for(int i = 0;i < NNN;i++)
				//{
					//Entity a = _store.getEntityById("Author",1);
					//String fn = R(firstNames);
					//a.setAttribute("FirstName",fn);
					//_store.saveEntity(a);
				//	insert_entity_instances(50);
				//}
			}catch(Exception e)
			{
				System.err.println("U");
				e.printStackTrace();
			}
		}
	}


	public void pssql_test() throws PersistenceException
	{
		insert_entity_instances(500);
		String pssql = "SELECT * FROM Author;";// WHERE (Weight < 2.0 OR Weight > 5) AND FirstName > 'Ez';";
		QueryResult result =null;
		
		t1 = System.currentTimeMillis();
		result = _store.executePSSqlQuery(pssql);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println(" PSSQL: "+pssql+" " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
	
	}
	
	public void shutdown_test() 
	{
		Runtime.getRuntime().addShutdownHook(new Thread(){
			
			public void run()
			{
				System.out.println("SHUTDOWN HOOK HIT");
				try{
				_store.close();
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
		});
		
		while(true)
			;
		
	}
	
	

	
	public void forensic_test() throws PersistenceException
	{
		List<EntityDefinition> defs = _store.getEntityDefinitions();
		for(int i = 0;i < defs.size();i++)
		{
			System.out.println(defs.get(i));
		}
		Query q;
		QueryResult result;
		q = new Query("User");
		q.idx(Query.PRIMARY_IDX);
		q.eq(Query.VAL_GLOB);

		t1 = System.currentTimeMillis();
		result  = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		for(int i = 0; i< result.size();i++)
		{
			Entity e = result.getEntities().get(i);
			System.out.println("ID: "+e.getId()+" EMAIL: "+e.getAttributeAsString("email")+" ROLE: "+e.getAttributeAsString("role")+" DATE CREATED:"+e.getAttributeAsString("date_created")+" LAST_MODIFIED: "+new Date(Long.MAX_VALUE-((Date)e.getAttribute("last_modified")).getTime()));
		}
	
		q = new Query("OutstandingEmailConfirmation");
		q.idx(Query.PRIMARY_IDX);
		q.eq(Query.VAL_GLOB);

		t1 = System.currentTimeMillis();
		result  = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		System.out.println("GLOB.....RESULTS");
		for(int i = 0; i< result.size();i++)
		{
			Entity e = result.getEntities().get(i);
			System.out.println("ID: "+e.getId()+" TYPE: "+e.getAttributeAsString("type")+" UID: "+e.getAttributeAsString("activation_uid")+" TOKEN: "+e.getAttributeAsString("activation_token"));
		}
		
		Date d = new Date();
		q = new Query("OutstandingEmailConfirmation");
		
		q.idx("byDateCreatedByType");
		System.out.println("IDX.....RESULTS");
		q.between(q.list(Query.VAL_MIN,25),q.list(d,25));
		q.cacheResults(false);

		t1 = System.currentTimeMillis();
		result  = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		for(int i = 0; i< result.size();i++)
		{
			Entity e = result.getEntities().get(i);
			System.out.println("ID: "+e.getId()+" TYPE: "+e.getAttributeAsString("type")+" UID: "+e.getAttributeAsString("activation_uid")+" TOKEN: "+e.getAttributeAsString("activation_token"));
		}

	}
	
	public void pkey_query_test() throws PersistenceException
	{
		insert_entity_instances(500);
		Query q;
		QueryResult result;
		
		q = new Query("Author");
		q.idx(Query.PRIMARY_IDX);
		q.eq(Query.VAL_GLOB);

		t1 = System.currentTimeMillis();
		result  = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println(" PREDICATE ID EQ VAL GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		q = new Query("Author");
		q.idx(Query.PRIMARY_IDX);
		q.lte(Query.VAL_MAX);

		t1 = System.currentTimeMillis();
		result  = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println(" PREDICATE ID LTE VAL MAX " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		
		q = new Query("Author");
		q.idx(Query.PRIMARY_IDX);
		q.between(10,30);

		t1 = System.currentTimeMillis();
		result  = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println(" ID BETWEEN 10 AND 30 " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		
		int c;
		q = new Query("Author");
		q.idx(Query.PRIMARY_IDX);
		q.eq(Query.VAL_GLOB);

		t1 = System.currentTimeMillis();
		c  = _store.count(q);
		t2 = System.currentTimeMillis()-t1;
		System.out.println("COUNT FOR EQ GLOB ON PIDX IS "+c);
		
	}
	
	public void delete_test() throws PersistenceException
	{
		Entity[] books = new Entity[10];
		for(int i = 0;i < books.length;i++)
		{
			Entity b = _store.getEntityDefinition("Book").createInstance();
			b.setAttribute("Title", R(titles));
			b = _store.saveEntity(b);
			books[i] = b;
			System.out.println("BOOK "+i+" IS "+b);
		}
		addSingleFieldEntityIndex("Book", "Title", EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX, "byTitle", null);

		Query q;
		q = new Query("Book");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byTitle");
		q.eq(Query.VAL_GLOB);
		
		QueryResult result;
		result = _store.executeQuery(q);
		
		print_books(result);
		
		_store.deleteEntity(books[0]);
		
		result  = _store.executeQuery(q);
		System.out.println("ENTITIES "+result.size());
		print_books(result);
	
	}
	
	public void set_contains_all_test() throws PersistenceException
	{
		List<Entity> books_list = new ArrayList<Entity>();
		for(int i = 0;i < 2;i++)
		{
			Entity b = _store.getEntityDefinition("Book").createInstance();
			b.setAttribute("Title", R(titles));
			b = _store.saveEntity(b);
			books_list.add(b);

		}
		
		List<String> owners_list = new ArrayList<String>();
		owners_list.add("Tom");
		owners_list.add("Harry");

		addMultiFieldEntityIndex("Author", new String[]{"Books","FirstName"}, EntityIndex.TYPE_MULTIFIELD_ARRAY_MEMBERSHIP_INDEX, "byBooksByFirstName", null);
		addMultiFieldEntityIndex("Author", new String[]{"Owners","FirstName"}, EntityIndex.TYPE_MULTIFIELD_ARRAY_MEMBERSHIP_INDEX, "byOwnersByFirstName", null);
		
		Entity author = _store.getEntityDefinition("Author").createInstance();
		author.setAttribute("Books", books_list);
		author.setAttribute("FirstName", "Topher");
		author.setAttribute("LastName", "LaFata");
		author.setAttribute("Owners", owners_list);
		author = _store.saveEntity(author);
		
		Entity book_1 = books_list.get(0);
		Entity book_2 = books_list.get(1);
		
		Query q;
		QueryResult result;

		System.out.println();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byOwnersByFirstName");
		q.setContainsAll(q.list(q.list("Tom","Harry"),Query.VAL_GLOB));
		result = _store.executeQuery(q);
		print(result);
		System.out.println("TOM, HARRY RESULT SIZE IS "+result.size());
		System.out.println();
		
		System.out.println();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byOwnersByFirstName");
		q.setContainsAll(q.list(q.list("Harry","Tom"),Query.VAL_GLOB));
		result = _store.executeQuery(q);
		print(result);
		System.out.println("HARRY,TOM RESULT SIZE IS "+result.size());
		System.out.println();
		
		
		System.out.println();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byBooksByFirstName");
		q.setContainsAll(q.list(q.list(book_1,book_2),Query.VAL_GLOB));
		result = _store.executeQuery(q);
		print(result);
		System.out.println("BOOKS RESULT SIZE IS "+result.size());
		System.out.println();
		
		System.out.println();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byBooksByFirstName");
		q.setContainsAll(q.list(q.list(book_2,book_1),Query.VAL_GLOB));
		result = _store.executeQuery(q);
		print(result);
		System.out.println("BOOKS RESULT SIZE IS "+result.size());
		System.out.println();
	
	}
	
	
	public void order_by_test() throws PersistenceException
	{
		insert_entity_instances(500);
		addSingleFieldEntityIndex("Author", "FirstName", EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX, "byFirstName", null);

		Query q;
		QueryResult result;
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.eq(Query.VAL_GLOB);
		q.orderBy("Birthday");
		result = _store.executeQuery(q);
		print(result);
		System.out.println("ORDER BY BIRTHDAY");
	}
	
	
	public void double_query_test() throws PersistenceException
	{
		insert_entity_instances(500);
		addMultiFieldEntityIndex("Author", new String[]{"FirstName","LastName"}, EntityIndex.TYPE_SIMPLE_MULTI_FIELD_INDEX, "byFirstNameByLastName", null);

		Query q;
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstNameByLastName");
		q.eq(q.list("Gigi",Query.VAL_GLOB));
	
		int c; 
		QueryResult result;
		System.out.println("1 \n"+q);
		result = _store.executeQuery(q);	
		 c = _store.count(q);	
		 print(result);
		System.out.println("C IS "+c);
		System.out.println("2 \n"+q);
		result = _store.executeQuery(q);	
		 c = _store.count(q);	
		print(result);
		System.out.println("C IS "+c);
		System.out.println("3 \n"+q);
	}
	
	public void count_test() throws PersistenceException
	{
		insert_entity_instances(500);
		addSingleFieldEntityIndex("Author", "FirstName", EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX, "byFirstName", null);
		Query q;

	    t1 = System.currentTimeMillis();	
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.eq("Gigi");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
	
		t1 = System.currentTimeMillis();
		int c = _store.count(q);
		t2 = System.currentTimeMillis()-t1;
		System.out.println("COUNT ALL GIGIs "+c+" "+t2+" ms");
	}
	
	public void string_min_test() throws PersistenceException
	{
		insert_entity_instances(500);
		addSingleFieldEntityIndex("Author", "FirstName", EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX, "byFirstName", null);
		Query q;

	    t1 = System.currentTimeMillis();	
		q = new Query("Author");
		//q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.gte(Query.VAL_MIN);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
	
		t1 = System.currentTimeMillis();
		QueryResult result  = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println(" PREDICATE FirstName GTE VAL_MIN " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
	}
	
	public void intersect_test() throws PersistenceException
	{
		insert_entity_instances(500);
		addSingleFieldEntityIndex("Author", "FirstName", EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX, "byFirstName", null);
		Query q;
		QueryResult result;
		

	    t1 = System.currentTimeMillis();	
		q = new Query("Author");
		
		q.idx("byFirstName");
		q.startIntersection();
			q.lt("Gigi");
			q.gt("Daya");
		q.endIntersection();
		q.pageSize(5);
		q.orderBy("LastName");
		q.ret();
		
		
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("INTERSECT PREDICATE LT GIGI AND GT Daya ORDERED BY LastName " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
	}
	
	public void union_test() throws PersistenceException
	{
		insert_entity_instances(500);
		addSingleFieldEntityIndex("Author", "FirstName", EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX, "byFirstName", null);
		Query q;
		QueryResult result;
		

	    t1 = System.currentTimeMillis();	
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.startUnion();
			q.lt("Gigi");
			q.gt("Daya");
		q.endUnion();
		
		q.orderBy("LastName");
		q.ret();
		
		
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("UNION PREDICATE LT GIGI AND GT Daya ORDERED BY LastName " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
	}

	public void single_paging_predicate_test() throws PersistenceException
	{
		insert_entity_instances(500);
		addSingleFieldEntityIndex("Author", "FirstName", EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX, "byFirstName", null);
		Query q;
		QueryResult result;
		

	    t1 = System.currentTimeMillis();	
		q = new Query("Author");
		q.pageSize(25);
		q.idx("byFirstName");
		q.eq("Gigi");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
	
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PAGING 25 PREDICATE EQ GIGI " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
	    t1 = System.currentTimeMillis();	
		q = new Query("Author");
		q.offset(25);
		q.pageSize(25);
		q.idx("byFirstName");
		q.eq("Gigi");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
	
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PAGING 25 PREDICATE EQ GIGI " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));

	    t1 = System.currentTimeMillis();	
		q = new Query("Author");
		q.offset(50);
		q.pageSize(25);
		q.idx("byFirstName");
		q.eq("Gigi");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
	
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PAGING 25 PREDICATE EQ GIGI " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		
	    t1 = System.currentTimeMillis();	
		q = new Query("Author");
		q.idx("byFirstName");
		q.eq("Gigi");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
	
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PAGING ALL RESULTS PREDICATE EQ GIGI " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));

	}
	
	public void single_predicate_test() throws PersistenceException
	{
		insert_entity_instances(500);
		addSingleFieldEntityIndex("Author", "FirstName", EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX, "byFirstName", null);
		Query q;
		QueryResult result;
		

	    t1 = System.currentTimeMillis();	
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.eq("Gigi");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
	
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PREDICATE EQ GIGI " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.gt("Gigi");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PREDICATE GT GIGI" + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.gte("Gigi");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PREDICATE GTE GIGI" + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.lt("Gigi");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PREDICATE LT GIGI" + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.lte("Gigi");	
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PREDICATE LTE GIGI" + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
	
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.startsWith("Gig");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PREDICATE STARTSWITH GIG " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));

		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.gte(Query.VAL_MIN);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PREDICATE GT VAL_MIN " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.lt(Query.VAL_MAX);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PREDICATE LT VAL_MAX " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.gte(Query.VAL_MIN);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PREDICATE GTE VAL_MIN " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.lt(Query.VAL_MAX);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PREDICATE LT VAL_MAX " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
	
		
		/* test globs */

	    t1 = System.currentTimeMillis();	
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.eq(Query.VAL_GLOB);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PREDICATE EQ GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.gt(Query.VAL_GLOB);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PREDICATE GT GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.gte(Query.VAL_GLOB);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PREDICATE GTE GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.lt(Query.VAL_GLOB);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PREDICATE LT GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.lte(Query.VAL_GLOB);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PREDICATE LTE GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
	
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.startsWith(Query.VAL_GLOB);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
	
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("SINGLE PREDICATE STARTSWITH GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
	
	}
	
	
	
	
	public void multi_predicate_test() throws PersistenceException
	{
		insert_entity_instances(500);
		Query q;
		QueryResult result;
		ArrayList<Object> params = new ArrayList<Object>();
		addMultiFieldEntityIndex("Author", new String[]{"FirstName","Birthday"}, EntityIndex.TYPE_SIMPLE_MULTI_FIELD_INDEX, "byFirstNamebyBirthday", null);
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstNamebyBirthday");
		params.add("Gigi");
		params.add(Query.VAL_GLOB);
		q.eq(params);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("MULTI PREDICATE EQ GIGI,GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstNamebyBirthday");
		params = new ArrayList<Object>();
		params.add("Gigi");
		params.add(Query.VAL_GLOB);
		q.gt(params);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("MULTI PREDICATE GT GIGI,GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstNamebyBirthday");
		params = new ArrayList<Object>();
		params.add("Gigi");
		params.add(Query.VAL_GLOB);
		q.gte(params);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("MULTI PREDICATE GTE GIGI,GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstNamebyBirthday");
		params = new ArrayList<Object>();
		params.add("Gigi");
		params.add(Query.VAL_GLOB);
		q.lt(params);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("MULTI PREDICATE LT GIGI,GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstNamebyBirthday");
		params = new ArrayList<Object>();
		params.add("Gigi");
		params.add(Query.VAL_GLOB);
		q.lte(params);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("MULTI PREDICATE LTE GIGI,GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstNamebyBirthday");
		params = new ArrayList<Object>();
		params.add("Gigi");
		params.add(Query.VAL_GLOB);
		q.startsWith(params);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("MULTI PREDICATE STARTSWITH GIGI,GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
	}
	
	public void multi_predicate_test_reference() throws PersistenceException
	{
		insert_entity_instances(500);
		Query q;
		QueryResult result;

		addMultiFieldEntityIndex("Author", new String[]{"FavoriteBook","FavoriteNumber"}, EntityIndex.TYPE_SIMPLE_MULTI_FIELD_INDEX, "byFavoriteBookByFavoriteNumber", null);
		addMultiFieldEntityIndex("Author", new String[]{"FavoriteNumber","FavoriteBook"}, EntityIndex.TYPE_SIMPLE_MULTI_FIELD_INDEX, "byFavoriteNumberByFavoriteBook", null);
		addMultiFieldEntityIndex("Author", new String[]{"FavoriteBook","WorkflowStatus"}, EntityIndex.TYPE_SIMPLE_MULTI_FIELD_INDEX, "byFavoriteBookByWorkflowStatus", null);
		addMultiFieldEntityIndex("Author", new String[]{"WorkflowStatus","FavoriteBook"}, EntityIndex.TYPE_SIMPLE_MULTI_FIELD_INDEX, "byWorkflowStatusByFavoriteBook", null);
		addMultiFieldEntityIndex("Author", new String[]{"WorkflowStatus","Books"}, EntityIndex.TYPE_MULTIFIELD_ARRAY_MEMBERSHIP_INDEX, "byWorkflowStatusByBooks", null);
		addMultiFieldEntityIndex("Author", new String[]{"Books","Birthday"}, EntityIndex.TYPE_MULTIFIELD_ARRAY_MEMBERSHIP_INDEX, "byBooksByBirthday", null);
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFavoriteBookByFavoriteNumber");
		q.eq(q.list(favoriteBooks[0],Query.VAL_GLOB));
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("MULTI PREDICATE EQ FavoriteBook[0],FAV NUM: GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFavoriteNumberByFavoriteBook");
		q.eq(q.list(2,Query.VAL_GLOB));
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("MULTI PREDICATE EQ FAV NUM:2 FavoriteBook: GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFavoriteBookByWorkflowStatus");
		q.eq(q.list(favoriteBooks[0],Query.VAL_GLOB));
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("MULTI PREDICATE EQ FavoriteBook[0],WorkflowStatus: GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byWorkflowStatusByFavoriteBook");
		q.eq(q.list("Published",Query.VAL_GLOB));
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("MULTI PREDICATE EQ WorkflowStatus:published FavoriteBook: GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byWorkflowStatusByFavoriteBook");
		q.eq(q.list(Query.VAL_GLOB,Query.VAL_GLOB));
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("MULTI PREDICATE EQ WorkflowStatus:GLOB FavoriteBook: GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byWorkflowStatusByBooks");
		q.setContainsAny(q.list("Published",q.list(favoriteBooks[0])));
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print_with_books(result);
		System.out.println("MULTI PREDICATE SET CONTAINS ANY WorkflowStatus:Published BOOKS: favoriteBooks[0] " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));

		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byWorkflowStatusByBooks");
		q.setContainsAll(q.list("Published",q.list(favoriteBooks[0],favoriteBooks[1])));
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print_with_books(result);
		System.out.println("MULTI PREDICATE SET CONTAINS ALL WorkflowStatus:Published BOOKS: favoriteBooks[0] favoriteBooks[1]" + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byWorkflowStatusByBooks");
		q.setContainsAny(q.list(Query.VAL_GLOB,Query.VAL_GLOB));
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print_with_books(result);
		System.out.println("MULTI PREDICATE SET CONTAINS ANY WorkflowStatus:GLOB BOOKS: GLOB" + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byBooksByBirthday");
		q.setContainsAny(q.list(q.list(favoriteBooks[0]),Query.VAL_GLOB));
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print_with_books(result);
		System.out.println("MULTI PREDICATE SET CONTAINS ANY BOOKS: favoriteBooks[0] BIRTHDAY: GLOB" + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		t1 = System.currentTimeMillis();
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byBooksByBirthday");
		q.setContainsAny(q.list(Query.VAL_GLOB,Query.VAL_GLOB));
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print_with_books(result);
		System.out.println("MULTI PREDICATE SET CONTAINS ANY BOOKS: GLOB BIRTHDAY: GLOB" + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));

			
	}
	
	
	public void range_test() throws PersistenceException
	{
		insert_entity_instances(500);
		addSingleFieldEntityIndex("Author", "FirstName", EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX, "byFirstName", null);
		t1 = System.currentTimeMillis();	

		Query q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.between("Daya", "Gigi");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		QueryResult result;
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("BETWEEN ASC INCLUSIVE DAYA,GIGI " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));

		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.betweenDesc("Gigi", "Daya");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("BETWEEN DESC INCLUSIVE DAYA,GIGI " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.betweenExclusive("Daya", "Gigi");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("BETWEEN ASC EXCLUSIVE DAYA,GIGI " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.betweenExclusiveDesc("Gigi", "Daya");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("BETWEEN DESC EXCLUSIVE GIGI,DAYA " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.betweenEndInclusive("Daya", "Gigi");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("BETWEEN ASC END INCLUSIVE DAYA,GIGI " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.betweenEndInclusiveDesc("Gigi", "Daya");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("BETWEEN DESC END INCLUSIVE GIGI,DAYA " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.betweenStartInclusive("Daya", "Gigi");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("BETWEEN ASC START INCLUSIVE DAYA,GIGI " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		

		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.betweenStartInclusiveDesc("Gigi", "Daya");
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis()-t1;
		print(result);
		System.out.println("BETWEEN DESC START INCLUSIVE GIGI,DAYA " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
	}
	
	public void array_index_test() throws PersistenceException
	{
		insert_entity_instances(500);
		addSingleFieldEntityIndex("Author", "Owners",EntityIndex.TYPE_ARRAY_MEMBERSHIP_INDEX, "byOwnerSubset",null);	
		
		ArrayList<Object> qset = new ArrayList<Object>();	
		qset.add("Joyce");
		qset.add("Wright");

		t1 = System.currentTimeMillis();	
		Query q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byOwnerSubset");
		q.setContainsAny(qset);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		QueryResult result;
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis() - t1;
		print(result);
		System.out.println("ARRAY MEMBERSHIP(Joyce,Wright) " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
	}
	
	public void multi_array_index_test() throws PersistenceException
	{
		insert_entity_instances(500);
		addMultiFieldEntityIndex("Author", new String[]{"Owners","WorkflowStatus"}, EntityIndex.TYPE_MULTIFIELD_ARRAY_MEMBERSHIP_INDEX, "byOwnersByWorkflowStatus",null);	
		ArrayList<Object> owners = new ArrayList<Object>();	
		owners.add("Joyce");
		//owners.add("Baker");
		owners.add("Wright");

		QueryResult result;
		Query q;
		

		ArrayList<Object> qset = new ArrayList<Object>();
		/*
		qset.clear();
		qset.add(owners);
		qset.add(Query.VAL_GLOB);//"Published");
		
		t1 = System.currentTimeMillis();	
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byOwnersByBirthday");
		q.setContainsAny(qset);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis() - t1;
		print(result);
		System.out.println("MULTI ARRAY MEMBERSHIP CONTAINS ANY(Joyce,Baker,Wright),GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
		*/

		qset.add(owners);
		qset.add(Query.VAL_GLOB);//"Published");
		t1 = System.currentTimeMillis();	
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byOwnersByWorkflowStatus");
		q.setContainsAll(qset);
		q.ret();
		t2 = System.currentTimeMillis()-t1;
		System.out.println("QUERY COMPILE TOOK "+(t2));
		
		
		t1 = System.currentTimeMillis();
		result = _store.executeQuery(q);
		t2 = System.currentTimeMillis() - t1;
		print(result);
		System.out.println("MULTI ARRAY MEMBERSHIP CONTAINS ALL [Joyce,Wright],GLOB " + t2 + " RESULT SIZE=" + result.size()+" RPS:"+((float)1000/t2*result.size()));
	}
	
	///CHANGE VARIABLE TO POINT TO A VALID DIRECTORY AND YOU SHOULD BE ABLE TO RUN TEST//
	public void init_store() throws PersistenceException
	{
		_store = new BDBStore();
		HashMap<Object, Object> config = new HashMap<Object, Object>();
		config.put(BDBStoreConfigKeyValues.KEY_STORE_ROOT_DIRECTORY , "C:/eclipse_workspace/PSPersistence/dbEnv");
		//config.put(BDBStoreConfigKeyValues.KEY_STORE_ROOT_DIRECTORY ,"C:/Users/David/Desktop/ll_080707/app-config/LimeLifeStore");

		config.put(
				BDBStoreConfigKeyValues.KEY_DEADLOCK_RESOLUTION_SCHEME,
				BDBStoreConfigKeyValues.VALUE_DEADLOCK_RESOLUTION_SCHEME_ALWAYS_CRAWL_LOCKTABLE);
		config.put(BDBStoreConfigKeyValues.KEY_STORE_LOCKER_CLASS, "com.pagesociety.bdb.locker.DefaultStoreLocker");
		_store.init(config);
	}

	
	public void insert_entity_definitions() throws PersistenceException
	{
		EntityDefinition def;
		def =  new EntityDefinition("Author");
		addEntityDefinition(def);
		def = new EntityDefinition("Book");
		addEntityDefinition(def);		
		FieldDefinition f;	
		f = new FieldDefinition("FirstName", 		Types.TYPE_STRING);
		addEntityField("Author", f, "Default FirstName");
		f = new FieldDefinition("LastName", 		Types.TYPE_STRING);
		addEntityField("Author", f, "Default LastName");
		f = new FieldDefinition("Weight", 			Types.TYPE_FLOAT);
		addEntityField("Author", f, new Float(160f));
		f = new FieldDefinition("IsHungry", 		Types.TYPE_BOOLEAN);
		addEntityField("Author", f, null);

		f = new FieldDefinition("Birthday", 		Types.TYPE_DATE);
		addEntityField("Author", f, null);
		
		f = new FieldDefinition("FavoriteNumber", Types.TYPE_INT);
		addEntityField("Author", f, null);
		f = new FieldDefinition("WorkflowStatus", Types.TYPE_STRING);
		addEntityField("Author", f, null);
		f = new FieldDefinition("Owners", 			Types.TYPE_STRING | Types.TYPE_ARRAY);
		addEntityField("Author", f, null);
		f = new FieldDefinition("Owners2", 			Types.TYPE_STRING | Types.TYPE_ARRAY);
		addEntityField("Author", f, null);
		f = new FieldDefinition("Ints", 			Types.TYPE_INT | Types.TYPE_ARRAY);
		addEntityField("Author", f, null);
		f = new FieldDefinition("Double", 			Types.TYPE_DOUBLE);
		addEntityField("Author", f, null);
		f = new FieldDefinition("Floats", 			Types.TYPE_FLOAT | Types.TYPE_ARRAY);
		addEntityField("Author", f, null);
		f = new FieldDefinition("BLOB", 			Types.TYPE_BLOB);
		addEntityField("Author", f, null);
		f = new FieldDefinition("FavoriteBook", 	Types.TYPE_REFERENCE, "Book");
		addEntityField("Author", f, null);
		f = new FieldDefinition("Books", Types.TYPE_REFERENCE | Types.TYPE_ARRAY, "Book");
		addEntityField("Author", f, null);
		f = new FieldDefinition("PrimaryBook", Types.TYPE_REFERENCE , "Book");
		addEntityField("Author", f, null);	
		
		f = new FieldDefinition("Title", Types.TYPE_STRING);
		addEntityField("Book", f, null);
		f = new FieldDefinition("Authors", Types.TYPE_REFERENCE | Types.TYPE_ARRAY, "Author");
		addEntityField("Book", f, null);	
		f = new FieldDefinition("PrimaryAuthor", Types.TYPE_REFERENCE , "Author");
		addEntityField("Book", f, null);	
	}
	
	static long DAY = 1000 * 60 * 60 * 24;	
	
	public List<Entity> insert_entity_instances(int NN) throws PersistenceException
	{
		
		for(int i = 0;i < favoriteBooks.length;i++)
		{
			Entity b = _store.getEntityDefinition("Book").createInstance();
			b.setAttribute("Title", R(titles));
			b = _store.saveEntity(b);
			favoriteBooks[i] = b;
			System.out.println("FavoriteBook "+i+" IS "+b);
		}
		
		int inserted = 0;
		long t = System.currentTimeMillis();
		//
		List<Entity> es = new ArrayList<Entity>();
		Date today = new Date();
		int gigi_count = 0;
		int frank_count = 0;
		int joyce_count = 0;
		int wright_count = 0;
		int joyce_wright_count = 0;
		int authors_with_favorite_book_0_and_no_2 = 0;
		HashMap<Long,Object> id_cache = new HashMap<Long,Object>();
		
		for (int i = 0; i < NN; i++)
		{
			try
			{
					Entity author = _store.getEntityDefinition("Author").createInstance();
					String first_name = Math.random() > .9 ? null : R(firstNames);
					//author.setAttribute("FirstName", "Gigi");
					author.setAttribute("FirstName", first_name);
					if(first_name != null && first_name.equals("Gigi"))
						gigi_count++;
					else if(first_name != null && first_name.equals("Frank"))
						frank_count++;
					author.setAttribute("LastName", R(lastNames));
					//author.setAttribute("FavoriteNumber", (int) (Math.random() * 1000));
					author.setAttribute("Birthday", new Date(today.getTime() - (long) (Math.random() * DAY * 32000)));
					author.setAttribute("IsHungry", Math.random() > .3);
					author.setAttribute("Weight", (float) Math.random() * 10);
		
					author.setAttribute("WorkflowStatus", Math.random() > .5 ? "Archived" :"Published");//"Published");
					// string array
					int seq = 0;
					List<String> owners;
					int nnn = (int)(Math.random() * 3 + 1);
					int n = (int)(Math.random() * 3 + 1);
					if(nnn == 222)
						owners = null;
					else
					{
						
						owners = new ArrayList<String>();
						
						//if(Math.random() > .5 )
						//{
						//	owners.add("Joyce");
						//	joyce_count++;
						//}else
						//{
						//	owners.add("Wright");
						//	wright_count++;
						//}
							
					
						
						for (int j = 0; j < n; j++)
						{
							String s = R(lastNames);
							if (!owners.contains(s))
							{
								owners.add(s);//continue;//owners.add(s+String.valueOf(seq++));
								if(s.equals("Joyce"))
								{
									joyce_count++;
									if(owners.contains("Wright"))
										joyce_wright_count++;
								}
								else if(s.equals("Wright"))
								{
									wright_count++;
									if(owners.contains("Joyce"))
										joyce_wright_count++;
								}
							}
						}
					
						//owners.add("Joyce");
						//owners.add("Wright");
					}
					
					//double r =  Math.random();
					//if(r > 0.8)
					//{
				//		owners.add("Joyce");
					//	owners.add("Wright");
					//}

					 
					author.setAttribute("Owners", owners);
					
					
					n = (int)(Math.random() * 6 + 1);
					List<String> owners2 = new ArrayList<String>();
					for (int j = 0; j < n; j++)
					{
						String s = R(lastNames);
						if (!owners2.contains(s))
							owners2.add(s);
					}
					author.setAttribute("Owners2", owners2);
					// ints
					if (Math.random() > .3)
					{
						n = (int) (Math.random() * 4 + 1);
						List<Integer> ints = new ArrayList<Integer>();
						for (int j = 0; j < n; j++)
							ints.add(j * 10);
						author.setAttribute("Ints", ints);
						
					}
					// floats
					n = (int) (Math.random() * 11 + 1);
					List<Float> floats = new ArrayList<Float>();
					for (int j = 0; j < n; j++)
						floats.add((float) (Math.random() * 5000));
					author.setAttribute("Floats", floats);
					// FavoriteBook //FavoriteNumber//
					if (Math.random() > .8)
					{	
						int fn = (int)(Math.random() * 3);
						author.setAttribute("FavoriteNumber",fn);
						author.setAttribute("FavoriteBook", R(favoriteBooks));
						if(fn == 2 && author.getAttribute("FavoriteBook") == favoriteBooks[0])
						{
							authors_with_favorite_book_0_and_no_2++;
						}
						inserted++;
					}
					// books
					if (Math.random() > .8)
					{
						n = (int) (Math.random() * 5 + 1);
						List<Entity> books = new ArrayList<Entity>();
						for (int j = 0; j < n; j++)
						{
							Entity book = R(favoriteBooks);//_store.getEntityDefinition("Book").createInstance();
							//book.setAttribute("Title", "My Boook " + j);
							books.add(book);
							//_store.saveEntity(book);
							inserted++;
						}
						author.setAttribute("Books", books);
					}
					//
			
						Entity eee = _store.saveEntity(author);
						if(id_cache.containsKey(eee.getId()))
						{
							System.out.println("\n\n!!!!!ERROR GETTING BACK DUP IDE FROM STORE !!!!!!\n\n");
						}
						id_cache.put(eee.getId(),eee);
						inserted++;

						es.add(author);
					}
				catch (Exception ee)
				{
					ee.printStackTrace();
					System.err.println("SAVE FAILED " + ee.getMessage());//
					continue;
				}
			}
		
		
		long tt = (System.currentTimeMillis() - t); 
		System.out.println(">" + inserted + " records inserted in " + tt + " ms" +" RPS: "+((float)1000/tt*inserted));
		System.out.println("GIGI COUNT "+gigi_count);
		System.out.println("FRANK COUNT "+frank_count);
		System.out.println("JOYCE COUNT "+joyce_count);
		System.out.println("WRIGHT COUNT "+wright_count);
		System.out.println("JOYCE/WRIGHT COUNT "+joyce_wright_count);
		System.out.println("FAV NUM=2 and FAV BOOK= fav books[0] "+authors_with_favorite_book_0_and_no_2);
		return es;
	}

	private String R(String[] s)
	{
		return s[(int) (Math.random() * s.length)];
	}
	
	private Entity R(Entity[] s)
	{
		return s[(int) (Math.random() * s.length)];
	}
	
	private void addEntityDefinition(EntityDefinition def) throws PersistenceException
	{
		try{
			_store.addEntityDefinition(def);
		}catch(PersistenceException pe)
		{
			System.out.println(def.getName()+" ALREADY EXISTS");
		}
	}
	
	private void addEntityField(String entity,FieldDefinition entity_field_def,Object default_value) throws PersistenceException
	{
		try{
		
			_store.addEntityField(entity, entity_field_def, default_value);
		}catch(PersistenceException pe)
		{
			System.out.println(entity_field_def.getName()+" ALREADY EXISTS");
		}
	}
	
	//addSingleFieldEntityIndex("Author", "Owners", "ArrayMembershipIndex", "byOwnerSubset",null);
	private void addSingleFieldEntityIndex(String entity,String field_name,int index_type,String index_name,Map<String,String> attributes) throws PersistenceException
	{
		try{
			_store.addEntityIndex(entity,field_name,index_type,index_name,attributes);		
		}catch(PersistenceException pe)
		{
			System.out.println(index_name+" ALREADY EXISTS");
		}
	}
	
	private void addMultiFieldEntityIndex(String entity,String[] field_names,int index_type,String index_name,Map<String,String> attributes) throws PersistenceException
	{
		try{	
			_store.addEntityIndex(entity,field_names,index_type,index_name,attributes);		
		}catch(PersistenceException pe)
		{
			System.out.println(index_name+" ALREADY EXISTS");
		}
	}
	
	private void print(QueryResult r)
	{
		for (Entity e : r.getEntities())
		{
			String fbid;
			Entity fb = (Entity)e.getAttribute("FavoriteBook"); 
			if(fb == null)
				fbid = null;
			else
				fbid = String.valueOf(fb.getId());
			System.out.println(">" + e.getId() + "\t" + e.getAttributeAsString("FirstName") + "\t" + e.getAttributeAsString("LastName") + "\t" + e.getAttributeAsString("Birthday")+"\t FavoriteBook Id:"+fbid
					+ "\tFAV NUM:" + e.getAttributeAsString("FavoriteNumber") + "\t" + e.getAttributeAsString("Weight") + "\t" + e.getAttributeAsString("WorkflowStatus")+"\t"+e.getAttributeAsString("Owners")+" "+e.getAttributeAsString("Owners2"));

		}
	}

	private void print_books(QueryResult r)
	{
		for (Entity e : r.getEntities())
		{
			System.out.println(">" + e.getId() +"\t" + e.getAttributeAsString("Title"));

		}
	}

	private void print_with_books(QueryResult r)
	{
		for (Entity e : r.getEntities())
		{
			String fbid;
			Entity fb = (Entity)e.getAttribute("FavoriteBook"); 
			if(fb == null)
				fbid = null;
			else
				fbid = String.valueOf(fb.getId());
			System.out.println(">" + e.getId() + "\t" + e.getAttributeAsString("FirstName") + "\t" + e.getAttributeAsString("LastName") + "\t" + e.getAttributeAsString("Birthday")+"\t FavoriteBook Id:"+fbid
					+ "\tFAV NUM:" + e.getAttributeAsString("FavoriteNumber") + "\t" + e.getAttributeAsString("Weight") + "\t" + e.getAttributeAsString("WorkflowStatus")+"\t"+e.getAttributeAsString("Owners")+" "+e.getAttributeAsString("Owners2"));
			System.out.println(e.getAttribute("Books"));
		}
	}

	private Entity[] favoriteBooks = new Entity[3];
	
	private String[] firstNames = new String[] { "Pilner", "Sumner", "Cloe", "Christine", "Carl", "Caleb","Coso", "Cubo", "Christopher", "Courtney",
"Daya", "Eugene", "Frank", "Gigi", "Janice", "Hortence", "Julie","Zeke" };
	private String[] lastNames = new String[] { "Smith", "Jones", "Joyce", "Lee", "Lawson", "Nonnes", "Allen", "Hernadez", "King", "Wright", "Lopez",
			"Garcia", "Baker", "Green", "Scott" };
	private String[] titles = new String[] {
			"The Adventures of Captain Bonneville",
			"The Adventures of Captain Bonneville, U.S.A. in the Rocky Mountains and the Far West: Digested from His Journals and Illustrated from Various Other Sources",
			"The Adventures of Don Quixote Vol. 2", "The Adventures of Gerard", "The Adventures of Hajji Baba of Ispahan",
			"The Adventures of Huckleberry Finn" };
}
