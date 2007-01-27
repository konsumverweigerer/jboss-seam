package org.jboss.seam.example.seamspace;

import javax.ejb.Local;

@Local
public interface BlogLocal
{
   void getBlog();
   
   void createComment();
   void saveComment();
   
   void createEntry();
   void saveEntry();
   
   void destroy();
}
