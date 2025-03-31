package com.example.laba3.server;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class DBHibernate implements com.example.laba3.server.DB {

    private final SessionFactory sf = HibernateSF.getSessionFactory();

    @Override
    public ArrayList<DBRecord> getAllRecords() {
        ArrayList<DBRecord> res = new ArrayList<>();
        List<DBRecord> list = sf.openSession().
                createQuery("FROM DBRecord obj ORDER BY obj.score DESC").list();
        res.addAll(list);
        return res;
    }

    @Override
    public void insOrUpdRecord(DBRecord rec) {
        Session session = sf.openSession();
        Transaction trn = session.beginTransaction();
        session.saveOrUpdate(rec);
        trn.commit();
        session.close();
    }
}
