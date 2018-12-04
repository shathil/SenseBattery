package ektara.com.BinObjectStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ektara.com.sensebattery.ResourseBin;

/**
 * Created by mohoque on 11/01/2017.
 */

public class SaveResourceBins {


    /*
    public static void writeAccount(ResourseBin resource,Boolean append) throws IOException, ClassNotFoundException
    {
        ObjectOutputStream oos=null;
        FileOutputStream fos=null;
        try
        {
            if(!append)
            {
                fos= new FileOutputStream(PPConstants.AllAccountDetails);
                oos = new ObjectOutputStream(fos);
                //System.out.println("Not Appending");
                oos.writeObject(resource);
            }
            else
            {
                File ff = new File(PPConstants.AllAccountDetails);
                if(!ff.exists())
                {
                    System.out.println("Required file not found");
                    return;
                }
                PPAccount aa=lookupAccount(account.getEmail());
                if(aa!=null)
                    throw new DuplicateAccountException("An Account already exits with this email-ID");
                oos = new AppendResourceObjects(new FileOutputStream(PPConstants.AllAccountDetails,append));
                oos.writeObject(resource);
            }
        }
        finally
        {
            if(oos!=null)
                oos.close();
            if(fos!=null)
                fos.close();
        }

    }


    public static ResourseBin lookupAccount(String email) throws IOException, ClassNotFoundException
    {
        PPAccount account = null; //initialize it after reading from file
        // write code to open the files, read
        PPAccount foundAccount=null;
        ObjectInputStream ois=null;
        FileInputStream fis=null;
        File ff = new File(PPConstants.AllAccountDetails);
        if(!ff.exists())
        {
            //System.out.println("Required file not found");
            return null;
        }
        try
        {
            fis=new FileInputStream(PPConstants.AllAccountDetails);
            ois = new ObjectInputStream(fis);
            while(fis.available()>0 && foundAccount==null)
            {
                //Object o=null;
                PPAccount ppa=null;
                try
                {
                    ppa = (PPAccount)ois.readObject();
                    if(ppa==null)
                        return null;
                    System.out.println(ppa);
                }

                catch(ClassCastException cce)
                {
                    System.out.println("Class cast exception "+cce.getCause());
                    cce.printStackTrace();
                }
                if(email.equals(ppa.getEmail()))
                {
                    foundAccount=ppa;
                    break;
                }
                if(ppa instanceof PPBusinessAccount)
                {
                    PPBusinessAccount ppba = (PPBusinessAccount)ppa;
                    ArrayList<PPRestrictedAccount> alist=ppba.getAccountOperators();
                    if(alist==null)
                        continue;
                    Iterator<PPRestrictedAccount> it = alist.iterator();
                    while(it.hasNext())
                    {
                        PPRestrictedAccount ppr=(PPRestrictedAccount) it.next();
                        System.out.println(ppr);
                        if(email.equals(ppr.getEmail()))
                        {
                            foundAccount = ppr;
                            break;
                        }
                    }//iterators while loop
                }//if it is a businessAccount
            }//outer while
        }//try
        finally
        {
            if(ois!=null)
                ois.close();
            if(fis!=null)
                fis.close();
        }
        return foundAccount;
    }
    */
}
