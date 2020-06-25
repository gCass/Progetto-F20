package main.webapp.servlet;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rythmengine.Rythm;

import main.model.configurator.ComponentCatalog;
import main.model.configurator.component.Component;
import main.services.persistence.PersistenceFacade;
import main.webapp.servlet.util.JsonMessages;

@SuppressWarnings("serial")
public class AdministratorServlet extends MyServlet {

	public AdministratorServlet(String name, String path) {
		super(name, path);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PersistenceFacade pf = PersistenceFacade.getIstance();
		String email = (String) request.getSession().getAttribute("email");
		ServletController controller = (ServletController) this.getServletConfig().getServletContext()
				.getAttribute(email + "_controller");

		if (email != null) {
			boolean isAdmin = controller.getCustomer().isAdmin();

			// Se nella sessione esiste la mail, mi salvo tutte le info e carico il profilo
			if (!isAdmin) {
				response.getWriter().write(Rythm.render("403.html"));
			} else {
				response.getWriter().write(Rythm.render("administrator.html", pf.getAllConstraints(),pf.getAdmin(),pf.getTypeComponent()));
			}
		} else {
			// altrimenti reindirizzo al login
			response.sendRedirect("/login");
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO aggiungere metodi JsonMessage
		String typeComponent = request.getParameter("typeComp");
		PersistenceFacade pf = PersistenceFacade.getIstance();
		ComponentCatalog catalog = ComponentCatalog.getInstance();
		String model = null;
		String type = null;
		double price = 0;

		if (request.getPathInfo().equals("/addComp")) {
			System.out.println("Salva");

			for (Entry<String, String[]> name : request.getParameterMap().entrySet()) {
				// pf.addAttribute(type, model, name.getKey(), name.getValue()[0]);
				// System.out.println(name.getKey() + " - " + name.getValue()[0]);
				JSONObject j = null;
				JSONParser jsonParser = new JSONParser();
				try {
					j = (JSONObject) jsonParser.parse(name.getKey());
				} catch (ParseException e) {
					e.printStackTrace();
				}

				System.out.println(j);

				model = (String) j.get("name");
				type = (String) j.get("type");
				System.out.println("Tipo � " + type);
				price = Double.parseDouble((String) j.get("price"));
				pf.addComponent(model, type, price);

				Iterator<String> o = j.keySet().iterator();
				String att;

				while (o.hasNext()) {
					att = o.next();
					System.out.println(att);
					if (!att.equals("price") && !att.equals("name") && !att.equals("type")) {
						pf.addAttribute(type, model, att, (String) j.get(att));
					}
				}

				catalog.refreshCatalog();
			}
		} else if (request.getPathInfo().equals("/removeComp")) {
			String[] results = request.getParameterValues("checkBox");

			for (String result : results) {
				model = result.split("@")[0];
				type = result.split("@")[1];
				
				System.out.println(model + " - " + type);
				
				pf.removeComponent(model, type);
			}

			catalog.refreshCatalog();
			response.sendRedirect("/administrator");
		} else if (request.getPathInfo().equals("/getCompForm")) {

			List<String> list = pf.getStandardAttributes(typeComponent);

			String json = "";
			json = JsonMessages.getJsonTypeComponentResponse(list);
			response.getWriter().write(json);

		} else if (request.getPathInfo().equals("/newTypeComp")) {
			String newTypeOfC = request.getParameter("newTypeOfC");
			String needed = request.getParameter("needed");
			
			boolean flag;
			if(needed !=null)
				if (needed.equals("on")) {
					flag = true;
				} else {
					flag = false;
				}
			else {
				flag = false;
			}
			
			String json = "";
			json = JsonMessages.getJsonNewTypeComponentResponse(pf.addTypeComponent(newTypeOfC, flag));
			response.getWriter().write(json);
		} else if (request.getPathInfo().equals("/getAllComp")) {
			System.out.println("Getallcomp");

			List<Component> list = catalog.getComponentListByType(typeComponent);
			String json = "";
			json = JsonMessages.getJsonAllTypeComponentResponse(list);
			response.getWriter().write(json);
		} else if (request.getPathInfo().equals("/checkAddAdmin")) {
			String mail=request.getParameter("email");
			String json = JsonMessages.getJsonNewTypeComponentResponse(pf.checkIfUserExist(mail));
			response.getWriter().write(json);
		}else if (request.getPathInfo().equals("/addAdmin")) {
			String mail=request.getParameter("email");
			String json = JsonMessages.getJsonNewTypeComponentResponse(pf.addAdmin(mail,true));
			response.getWriter().write(json);
		}else if (request.getPathInfo().equals("/removeAdmin")) {
			String mail=request.getParameter("email");
			String json = JsonMessages.getJsonNewTypeComponentResponse(pf.addAdmin(mail,false));
			response.getWriter().write(json);
		}else if (request.getPathInfo().equals("/addStdAtt")) {
			String name = request.getParameter("stdAttName");
			String stdAtttype = request.getParameter("stdAttType");
			String bound = request.getParameter("stdAttBound");
			String cat = request.getParameter("stdAttCat");
			String isPres = request.getParameter("stdAttIsPres");
			
			boolean flag;
			if(isPres !=null)
				if (isPres.equals("on")) {
					flag = true;
				} else {
					flag = false;
				}
			else {
				flag = false;
			}
			
			if(bound.equals("None")){
				bound = null;
			}
			
			if(cat.equals("None")){
				cat = null;
			}
			
			String json = "";
			json = JsonMessages.getJsonAddStdAttResponse(pf.addStandardAttribute(name, stdAtttype, bound, cat, flag));
			response.getWriter().write(json);
		}
	}
}
