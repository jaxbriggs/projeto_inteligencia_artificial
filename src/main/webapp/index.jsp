<%-- 
    Document   : index
    Created on : May 27, 2016, 12:41:02 AM
    Author     : Carlos Henrique
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Projeto IA</title>
    </head>
    <body>
        <h1 style="text-align: center;">Projeto IA</h1>
        <div style="text-align: center;">
            <form action="/GeradorServlet" method="post" enctype="multipart/form-data">
                <label for="kval">Valor de K: </label>
                <input type="number" name="kval" min="1" value="3"/><br/><br/>
                
                <label for="file">Arquivo .arff: </label>
                <input type="file" name="file" /><br/><br/>
                
                <input type="submit" value="Enviar"/>
            </form>
        </div>
    </body>
</html>
