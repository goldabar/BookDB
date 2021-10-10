package zad1;

import java.sql.Connection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



public class BookDB extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private DataSource dataS;

	public void init() throws ServletException {
		
		try {			
			Context ctx = new InitialContext();			
			Context c = (Context) ctx.lookup("java:comp/env");			
			dataS = (DataSource) c.lookup("jdbc/ksidb");			
		} catch (NamingException exc) {			
			throw new ServletException("Nie mozna znalezc bazy danych", exc);
		}
	}
	
	public void setHtmlCode(HttpServletRequest  req, HttpServletResponse  res, ResultSet  resSet) throws IOException, SQLException {	
		res.setContentType("text/html; charset=windows-1250");		
		PrintWriter pwout = res.getWriter();
		pwout.print(
				"<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css\">");
		pwout.print("<body><h1>Books</h1>");

		pwout.print(
				"<br>"
				+ "<div class=\"form\"><form method=\"GET\" action=\"getAutor\">"
				+ "&nbsp&nbsp<label for=\"auth\">Author:</label><input type=\"text\" name=\"autor\">"
				+ "<input type=\"submit\" name=\"butt-autor\" value=\"Search \"><br>" + "</form></div>"
		);
		pwout.print("<br><br><table><tr><th>ISBN</th><th>Author</th><th>Title</th><th>Publisher</th><th>Price</th></tr>");

		while (resSet.next()) {
			String isbn = resSet.getString(1);
			String author = resSet.getString(2);
			String title = resSet.getString(3);
			String publisher = resSet.getString(4);
			float price = resSet.getFloat(6);
			pwout.print("<tr><td>" + isbn + "</td><td>" + author + "</td><td>" + title + "</td><td>" + publisher
					+ "</td><td> $" + price + "</td></tr>");
		}
		pwout.print("</table></body>");
		pwout.close();
	}

	public void serviceRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {	
		resp.setContentType("text/html; charset=windows-1250");
		PrintWriter pwout = resp.getWriter();
		String s = "select ISBN, AUTOR.NAME, TYTUL, WYDAWCA.NAME, ROK, CENA from POZYCJE inner join AUTOR on POZYCJE.AUTID=AUTOR.AUTID inner join WYDAWCA on POZYCJE.WYDID=WYDAWCA.WYDID";
		Connection con;
		con = null;
		try {
			synchronized (dataS) {			
				con = dataS.getConnection();
			}			
			Statement st = con.createStatement();		
			ResultSet ress = st.executeQuery(s);
			setHtmlCode(req, resp, ress);
			ress.close();		
			st.close();			
		} catch (Exception exc) {			
			pwout.println(exc.getMessage());		
		} finally {			
			try {			
				con.close();				
			} catch (Exception exc) {
				
			}
		}

		pwout.close();

	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
	
		res.setContentType("text/html; charset=windows-1250");
		
		PrintWriter out = res.getWriter();

		if (req.getParameter("butt-autor") != null) {

			String author = (String) req.getParameter("autor");

			Connection con;
			
			con = null;

			try {
				
				synchronized (dataS) {
					
					con = dataS.getConnection();
				}
				String stat = "select ISBN, AUTOR.NAME, TYTUL, WYDAWCA.NAME, ROK, CENA from POZYCJE inner join AUTOR on POZYCJE.AUTID=AUTOR.AUTID " + "inner join WYDAWCA on POZYCJE.WYDID=WYDAWCA.WYDID" + " where AUTOR.NAME like '%" + author + "%'";
				Statement st = con.createStatement();				
				ResultSet rset = st.executeQuery(stat);
				setHtmlCode(req, res, rset);
				rset.close();
				st.close();

			} catch (Exception exc) {			
				out.println(exc.getMessage());				
			} finally {			
				try {				
					con.close();				
				} catch (Exception exc) {
					
				}
			}
			out.close();			
		} else {			
			serviceRequest(req, res);
		}
	}
}
