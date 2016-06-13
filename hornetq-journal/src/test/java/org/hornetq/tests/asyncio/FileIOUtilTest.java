/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hornetq.tests.asyncio;

import org.hornetq.core.journal.SequentialFile;
import org.hornetq.core.journal.SequentialFileFactory;
import org.hornetq.core.journal.impl.NIOSequentialFileFactory;
import org.hornetq.core.journal.util.FileIOUtil;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.ByteBuffer;

public class FileIOUtilTest
{

   @Rule
   public TemporaryFolder temporaryFolder;

   public FileIOUtilTest()
   {
      File parent = new File("./target");
      parent.mkdirs();
      temporaryFolder = new TemporaryFolder(parent);
   }

   @Test
   public void testCopy() throws Exception
   {
      System.out.println("Data at " + temporaryFolder.getRoot());
      SequentialFileFactory factory = new NIOSequentialFileFactory(temporaryFolder.getRoot().getPath());
      SequentialFile file = factory.createSequentialFile("file1.bin", 1);
      file.open();

      ByteBuffer buffer = ByteBuffer.allocate(204800);
      buffer.put(new byte[204800]);
      buffer.rewind();
      file.writeDirect(buffer, true);

      buffer = ByteBuffer.allocate(409605);
      buffer.put(new byte[409605]);
      buffer.rewind();

      SequentialFile file2 = factory.createSequentialFile("file2.bin", 1);

      file2.open();
      file2.writeDirect(buffer, true);


      // This is allocating a reusable buffer to perform the copy, just like it's used within LargeMessageInSync
      buffer = ByteBuffer.allocate(4 * 1024);

      SequentialFile newFile = factory.createSequentialFile("file1.cop", 1);
      FileIOUtil.copyData(file, newFile, buffer);

      SequentialFile newFile2 = factory.createSequentialFile("file2.cop", 1);
      FileIOUtil.copyData(file2, newFile2, buffer);

      Assert.assertEquals(file.size(), newFile.size());
      Assert.assertEquals(file2.size(), newFile2.size());

      newFile.close();
      newFile2.close();
      file.close();
      file2.close();

      System.out.println("Test result::");

   }


}
