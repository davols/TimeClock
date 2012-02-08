package se.dxapps.beta.timeclock.provider;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

/**
 * Simple class which uses the api to present the contents
 * of an excel 97 spreadsheet as comma separated values, using a workbook
 * and output stream of your choice
 */
public class CSV
{
  /**
   * Constructor
   *
   * @param w The workbook to interrogate
   * @param out The output stream to which the CSV values are written
   * @param encoding The encoding used by the output stream.  Null or 
   * unrecognized values cause the encoding to default to UTF8
   * @param hide Suppresses hidden cells
   * @exception java.io.IOException
   */
  public CSV(Workbook w, OutputStream out, String encoding, boolean hide)
    throws IOException
  {
    if (encoding == null || !encoding.equals("UnicodeBig"))
    {
      encoding = "UTF8";
    }

    try
    {
      OutputStreamWriter osw = new OutputStreamWriter(out, encoding);
      BufferedWriter bw = new BufferedWriter(osw);
      
      for (int sheet = 0; sheet < w.getNumberOfSheets(); sheet++)
      {
        Sheet s = w.getSheet(sheet);

        if (!(hide && s.getSettings().isHidden()))
        {
          bw.write("*** " + s.getName() + " ****");
          bw.newLine();
          
          Cell[] row = null;
          
          for (int i = 0 ; i < s.getRows() ; i++)
          {
            row = s.getRow(i);
            
            if (row.length > 0)
            {
              if (!(hide && row[0].isHidden()))
              {
                bw.write(row[0].getContents());
                // Java 1.4 code to handle embedded commas
                // bw.write("\"" + row[0].getContents().replaceAll("\"","\"\"") + "\"");
              }
              
              for (int j = 1; j < row.length; j++)
              {
                bw.write(',');
                if (!(hide && row[j].isHidden()))
                {
                  bw.write(row[j].getContents());
                  // Java 1.4 code to handle embedded quotes
                  //  bw.write("\"" + row[j].getContents().replaceAll("\"","\"\"") + "\"");
                }
              }
            }
            bw.newLine();
          }
        }
      }
      bw.flush();
      bw.close();
    }
    catch (UnsupportedEncodingException e)
    {
      System.err.println(e.toString());
    }
  }
}