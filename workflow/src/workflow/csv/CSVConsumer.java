//
//  CSVConsumer.java
//  Project waoapp
//
//  Created by ktsubaki on Wed Jan 16 2002.
//  Copyright (c) 2002 ArtesWAre. All rights reserved.
//

package workflow.csv;

import java.util.List;


/**

 *
 * @author  Kazuo Tsubaki
 * @version 1.1
 * @see  CSVReader   
 */

public interface CSVConsumer {

    /**
     */
    public abstract void consumeLineOfTokens(String path, int rowIndex, List<String> record) throws Exception;

}
