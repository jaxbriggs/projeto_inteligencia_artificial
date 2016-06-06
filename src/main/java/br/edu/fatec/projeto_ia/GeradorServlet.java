package br.edu.fatec.projeto_ia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import weka.core.Instances;
import static java.lang.System.out;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import weka.classifiers.lazy.IB1;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.Id3;
import weka.core.Instance;

/**
 *
 * @author Carlos Henrique
 */
@MultipartConfig
public class GeradorServlet extends HttpServlet {
    
    XSSFWorkbook workbook; 
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        //Pega o arquivo arff do cliente
        Part filePart = request.getPart("file");
        
        if(filePart.getSubmittedFileName().equals("statec-nominal.arff")){
            
            InputStream fileContent = filePart.getInputStream();

            File f = StreamUtil.stream2file(fileContent);

            FileReader leitor = new FileReader(f);

            Instances baseArff = new Instances(leitor);
            baseArff.setClassIndex(7);
            
            Id3 arvore = new Id3();
            
            Map<Integer, String> realResults = new TreeMap<>();

            Map<Integer, String> id3Results = new TreeMap<>();
            String[][] comparacoesId3 = new String[134][2];
            
            try{
                arvore.buildClassifier(baseArff);
                
                for(int i = 0; i < baseArff.numInstances(); i++){

                    Instance teste = baseArff.instance(i);
                    realResults.put(i, baseArff.classAttribute().value((int)teste.classValue()));
                    comparacoesId3[i][0] = baseArff.classAttribute().value((int)teste.classValue());

                    teste.setClassMissing();

                    //Id3
                    double classeId3 = arvore.classifyInstance(teste);
                    id3Results.put(i, baseArff.classAttribute().value((int)classeId3));
                    comparacoesId3[i][1] = baseArff.classAttribute().value((int)classeId3);
                    
                }                
                
                //System.out.println(arvore);
                
            } catch (Exception ex) {
                out.println("Erro ao classificar instancias!");
                ex.printStackTrace();
            }
            
            try
              {          
                FileInputStream file = new FileInputStream(new File(getServletContext().getRealPath("/") + "matriz_ia.xlsx"));

                workbook = new XSSFWorkbook (file);

                XSSFSheet avaliador = workbook.getSheetAt(0);
                XSSFSheet matriz = workbook.getSheetAt(1);

                int rowCount = 0, cellCount = 0;

                //Preenche os reais
                rowCount = 1; cellCount = 0;
                Iterator it3 = realResults.entrySet().iterator();
                while (it3.hasNext()) {

                    Map.Entry pair = (Map.Entry)it3.next();
                    XSSFRow row;

                    row = avaliador.getRow(rowCount);
                    Cell cell = row.getCell(cellCount);
                    cell.setCellValue(pair.getValue().toString());

                    rowCount++;
                    it3.remove(); 

                }

                //Preenche os resultados ID3
                rowCount = 1; cellCount = 3;
                Iterator it = id3Results.entrySet().iterator();
                while (it.hasNext()) {

                    Map.Entry pair = (Map.Entry)it.next();
                    XSSFRow row;

                    row = avaliador.getRow(rowCount);
                    Cell cell = row.getCell(cellCount);
                    cell.setCellValue(pair.getValue().toString());

                    rowCount++;
                    it.remove(); 
                }

                String[] classes = {"aneuploid", "diploid", "tetraploid"};

                //Comeca a preencher a matriz de confusao
                //ID3
                int[][] id3Confusao = new int[3][3];
                int aa = 0, ad = 0, at = 0,
                    da = 0, dd = 0, dt = 0,
                    ta = 0, td = 0, tt = 0;

                for(int j = 0; j < 134; j++){
                    //Aneuploide/X
                    if(classes[0].equals(comparacoesId3[j][0]) && classes[0].equals(comparacoesId3[j][1])){
                        aa++;
                    } else if(classes[0].equals(comparacoesId3[j][0]) && classes[1].equals(comparacoesId3[j][1])) {
                        ad++;
                    } else if(classes[0].equals(comparacoesId3[j][0]) && classes[2].equals(comparacoesId3[j][1])) {
                        at++;
                    }

                    //Diploide/X
                    if(classes[1].equals(comparacoesId3[j][0]) && classes[0].equals(comparacoesId3[j][1])){
                        da++;
                    } else if(classes[1].equals(comparacoesId3[j][0]) && classes[1].equals(comparacoesId3[j][1])) {
                        dd++;
                    } else if(classes[1].equals(comparacoesId3[j][0]) && classes[2].equals(comparacoesId3[j][1])) {
                        dt++;
                    }

                    //Tetraploide/X
                    if(classes[2].equals(comparacoesId3[j][0]) && classes[0].equals(comparacoesId3[j][1])){
                        ta++;
                    } else if(classes[2].equals(comparacoesId3[j][0]) && classes[1].equals(comparacoesId3[j][1])) {
                        td++;
                    } else if(classes[2].equals(comparacoesId3[j][0]) && classes[2].equals(comparacoesId3[j][1])) {
                        tt++;
                    }
                }

                id3Confusao[0][0] = aa;
                id3Confusao[0][1] = ad;
                id3Confusao[0][2] = at;

                id3Confusao[1][0] = da;
                id3Confusao[1][1] = dd;
                id3Confusao[1][2] = dt;

                id3Confusao[2][0] = ta;
                id3Confusao[2][1] = td;
                id3Confusao[2][2] = tt;                

                //Popula a matriz na planilha
                //ID3
                XSSFRow row;
                cellCount = 16;
                for(int i = 0; i < 3; i++){
                    row = matriz.getRow(2+i);
                    for(int j = 0; j < 3; j++){
                        Cell cell = row.getCell(cellCount);
                        cell.setCellValue(id3Confusao[i][j]);
                        cellCount++;
                    }
                    cellCount = 16;
                }

              } catch (FileNotFoundException e) 
               {
                 e.printStackTrace();
               } 
               catch (IOException e) 
               {
                 e.printStackTrace();
               }

           XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);

           response.setContentType("application/vnd.ms-excel");
           response.setHeader("Content-Disposition", "attachment; filename=matriz_confusao.xlsx");
           workbook.write(response.getOutputStream());
           response.getOutputStream().close();
            
        } else if(filePart.getSubmittedFileName().equals("statec.arff")) {
       
            InputStream fileContent = filePart.getInputStream();

            File f = StreamUtil.stream2file(fileContent);

            FileReader leitor = new FileReader(f);

            Instances baseArff = new Instances(leitor);
            baseArff.setClassIndex(7);
            //baseArff.resample(new Random());

            Map<Integer, String> realResults = new TreeMap<>();

            Map<Integer, String> ib1Results = new TreeMap<>();
            String[][] comparacoesIb1 = new String[146][2];
            String[][] comparacoesIbk = new String[146][2];

            Map<Integer, String> knnResults = new TreeMap<>();
            IB1 classificador = new IB1();
            IBk classificadorIbk = new IBk(Integer.parseInt(request.getParameter("kval")));

            try {
                for(int i = 0; i < baseArff.numInstances(); i++){

                    Instances exemplosTeste = baseArff.testCV(baseArff.numInstances(), i);
                    Instances exemplosTreino = baseArff.trainCV(baseArff.numInstances(), i);

                    classificador.buildClassifier(exemplosTreino);
                    classificadorIbk.buildClassifier(exemplosTreino);

                    Instance teste = exemplosTeste.instance(0);
                    realResults.put(i, baseArff.classAttribute().value((int)teste.classValue()));
                    comparacoesIb1[i][0] = baseArff.classAttribute().value((int)teste.classValue());
                    comparacoesIbk[i][0] = baseArff.classAttribute().value((int)teste.classValue());

                    teste.setClassMissing();

                    //Ib1
                    double classeIb1 = classificador.classifyInstance(teste);
                    ib1Results.put(i, baseArff.classAttribute().value((int)classeIb1));
                    comparacoesIb1[i][1] = baseArff.classAttribute().value((int)classeIb1);

                    //Knn
                    double classeIbk = classificadorIbk.classifyInstance(teste);
                    knnResults.put(i, baseArff.classAttribute().value((int)classeIbk));
                    comparacoesIbk[i][1] = baseArff.classAttribute().value((int)classeIbk);
                }

            } catch (Exception ex) {
                out.println("Erro ao classificar instancias!");
                ex.printStackTrace();
            }        

            try
              {          
                FileInputStream file = new FileInputStream(new File(getServletContext().getRealPath("/") + "matriz_ia.xlsx"));

                workbook = new XSSFWorkbook (file);

                XSSFSheet avaliador = workbook.getSheetAt(0);
                XSSFSheet matriz = workbook.getSheetAt(1);

                int rowCount = 0, cellCount = 0;

                //Preenche os reais
                rowCount = 1; cellCount = 0;
                Iterator it3 = realResults.entrySet().iterator();
                while (it3.hasNext()) {

                    Map.Entry pair = (Map.Entry)it3.next();
                    XSSFRow row;

                    row = avaliador.getRow(rowCount);
                    Cell cell = row.getCell(cellCount);
                    cell.setCellValue(pair.getValue().toString());

                    rowCount++;
                    it3.remove(); 

                }

                //Preenche os resultados knn
                rowCount = 1; cellCount = 1;
                Iterator it = knnResults.entrySet().iterator();
                while (it.hasNext()) {

                    Map.Entry pair = (Map.Entry)it.next();
                    XSSFRow row;

                    row = avaliador.getRow(rowCount);
                    Cell cell = row.getCell(cellCount);
                    cell.setCellValue(pair.getValue().toString());

                    rowCount++;
                    it.remove(); 
                }

                //Preenche os resultados vizinho mais proximo
                rowCount = 1; cellCount = 2;
                Iterator it2 = ib1Results.entrySet().iterator();
                while (it2.hasNext()) {

                    Map.Entry pair = (Map.Entry)it2.next();
                    XSSFRow row;

                    row = avaliador.getRow(rowCount);
                    Cell cell = row.getCell(cellCount);
                    cell.setCellValue(pair.getValue().toString());

                    rowCount++;
                    it2.remove(); 
                }

                String[] classes = {"aneuploid", "diploid", "tetraploid"};

                //Comeca a preencher a matriz de confusao

                //KNN
                int[][] ibkConfusao = new int[3][3];
                int aa = 0, ad = 0, at = 0,
                    da = 0, dd = 0, dt = 0,
                    ta = 0, td = 0, tt = 0;

                for(int j = 0; j < 146; j++){
                    //Aneuploide/X
                    if(classes[0].equals(comparacoesIbk[j][0]) && classes[0].equals(comparacoesIbk[j][1])){
                        aa++;
                    } else if(classes[0].equals(comparacoesIbk[j][0]) && classes[1].equals(comparacoesIbk[j][1])) {
                        ad++;
                    } else if(classes[0].equals(comparacoesIbk[j][0]) && classes[2].equals(comparacoesIbk[j][1])) {
                        at++;
                    }

                    //Diploide/X
                    if(classes[1].equals(comparacoesIbk[j][0]) && classes[0].equals(comparacoesIbk[j][1])){
                        da++;
                    } else if(classes[1].equals(comparacoesIbk[j][0]) && classes[1].equals(comparacoesIbk[j][1])) {
                        dd++;
                    } else if(classes[1].equals(comparacoesIbk[j][0]) && classes[2].equals(comparacoesIbk[j][1])) {
                        dt++;
                    }

                    //Tetraploide/X
                    if(classes[2].equals(comparacoesIbk[j][0]) && classes[0].equals(comparacoesIbk[j][1])){
                        ta++;
                    } else if(classes[2].equals(comparacoesIbk[j][0]) && classes[1].equals(comparacoesIbk[j][1])) {
                        td++;
                    } else if(classes[2].equals(comparacoesIbk[j][0]) && classes[2].equals(comparacoesIbk[j][1])) {
                        tt++;
                    }
                }

                ibkConfusao[0][0] = aa;
                ibkConfusao[0][1] = ad;
                ibkConfusao[0][2] = at;

                ibkConfusao[1][0] = da;
                ibkConfusao[1][1] = dd;
                ibkConfusao[1][2] = dt;

                ibkConfusao[2][0] = ta;
                ibkConfusao[2][1] = td;
                ibkConfusao[2][2] = tt;

                //KNN
                int[][] ib1Confusao = new int[3][3];
                aa = 0; ad = 0; at = 0;
                da = 0; dd = 0; dt = 0;
                ta = 0; td = 0; tt = 0;

                for(int j = 0; j < 146; j++){
                    //Aneuploide/X
                    if(classes[0].equals(comparacoesIb1[j][0]) && classes[0].equals(comparacoesIb1[j][1])){
                        aa++;
                    } else if(classes[0].equals(comparacoesIb1[j][0]) && classes[1].equals(comparacoesIb1[j][1])) {
                        ad++;
                    } else if(classes[0].equals(comparacoesIb1[j][0]) && classes[2].equals(comparacoesIb1[j][1])) {
                        at++;
                    }

                    //Diploide/X
                    if(classes[1].equals(comparacoesIb1[j][0]) && classes[0].equals(comparacoesIb1[j][1])){
                        da++;
                    } else if(classes[1].equals(comparacoesIb1[j][0]) && classes[1].equals(comparacoesIb1[j][1])) {
                        dd++;
                    } else if(classes[1].equals(comparacoesIb1[j][0]) && classes[2].equals(comparacoesIb1[j][1])) {
                        dt++;
                    }

                    //Tetraploide/X
                    if(classes[2].equals(comparacoesIb1[j][0]) && classes[0].equals(comparacoesIb1[j][1])){
                        ta++;
                    } else if(classes[2].equals(comparacoesIb1[j][0]) && classes[1].equals(comparacoesIb1[j][1])) {
                        td++;
                    } else if(classes[2].equals(comparacoesIb1[j][0]) && classes[2].equals(comparacoesIb1[j][1])) {
                        tt++;
                    }
                }

                ib1Confusao[0][0] = aa;
                ib1Confusao[0][1] = ad;
                ib1Confusao[0][2] = at;

                ib1Confusao[1][0] = da;
                ib1Confusao[1][1] = dd;
                ib1Confusao[1][2] = dt;

                ib1Confusao[2][0] = ta;
                ib1Confusao[2][1] = td;
                ib1Confusao[2][2] = tt;

                //Popula a matriz na planilha
                //Knn
                XSSFRow row;
                cellCount = 2;
                for(int i = 0; i < 3; i++){
                    row = matriz.getRow(2+i);
                    for(int j = 0; j < 3; j++){
                        Cell cell = row.getCell(cellCount);
                        cell.setCellValue(ibkConfusao[i][j]);
                        cellCount++;
                    }
                    cellCount = 2;
                }

                //Vizinho mais proximo
                cellCount = 9;
                for(int i = 0; i < 3; i++){
                    row = matriz.getRow(2+i);
                    for(int j = 0; j < 3; j++){
                        Cell cell = row.getCell(cellCount);
                        cell.setCellValue(ib1Confusao[i][j]);
                        cellCount++;
                    }
                    cellCount = 9;
                }

              } catch (FileNotFoundException e) 
               {
                 e.printStackTrace();
               } 
               catch (IOException e) 
               {
                 e.printStackTrace();
               }

           XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);

           response.setContentType("application/vnd.ms-excel");
           response.setHeader("Content-Disposition", "attachment; filename=matriz_confusao.xlsx");
           workbook.write(response.getOutputStream());
           response.getOutputStream().close();
        }
    }

    @Override
    public String getServletInfo() {
        return "Servlet que cria a matriz de confusão dinâmicamente.";
    }

}
