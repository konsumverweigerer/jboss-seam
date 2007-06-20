package org.jboss.seam.core;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.web.Session;

/**
 * Support for "breadcrumbs".
 * 
 * @author Gavin King
 */
@Scope(ScopeType.PAGE)
@Name("org.jboss.seam.core.conversationStack")
@Install(precedence=BUILT_IN)
@BypassInterceptors
public class ConversationStack implements Serializable 
{
   private static final long serialVersionUID = 7941458529299691801L;
   private List<ConversationEntry> conversationEntryStack;
   
   @Create
   public void createConversationEntryStack()
   {
      ConversationEntries conversationEntries = ConversationEntries.getInstance();
      if (conversationEntries==null)
      {
         conversationEntryStack = Collections.EMPTY_LIST;
      }
      else
      {
         ConversationEntry currentConversationEntry = Manager.instance().getCurrentConversationEntry();
         if (currentConversationEntry!=null)
         {
            List<String> idStack = currentConversationEntry.getConversationIdStack();
            conversationEntryStack = new ArrayList<ConversationEntry>( conversationEntries.size() );
            ListIterator<String> ids = idStack.listIterator( idStack.size() );
            while ( ids.hasPrevious() )
            {
               ConversationEntry entry = conversationEntries.getConversationEntry( ids.previous() );
               if ( entry.isDisplayable() && !Session.instance().isInvalid() ) 
               {
                  conversationEntryStack.add(entry);
               }
            }
         }
      }
   }
   
   @Unwrap
   public List<ConversationEntry> getConversationEntryStack()
   {
      return conversationEntryStack;
   }
   
}
