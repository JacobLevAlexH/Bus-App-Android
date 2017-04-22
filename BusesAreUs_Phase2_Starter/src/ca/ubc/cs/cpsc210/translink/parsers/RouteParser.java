package ca.ubc.cs.cpsc210.translink.parsers;

import ca.ubc.cs.cpsc210.translink.model.Route;
import ca.ubc.cs.cpsc210.translink.model.RouteManager;
import ca.ubc.cs.cpsc210.translink.model.RoutePattern;
import ca.ubc.cs.cpsc210.translink.parsers.exception.RouteDataMissingException;
import ca.ubc.cs.cpsc210.translink.providers.DataProvider;
import ca.ubc.cs.cpsc210.translink.providers.FileDataProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Parse route information in JSON format.
 */

//TODO:study this class
public class RouteParser {
    private String filename;

    public RouteParser(String filename) {
        this.filename = filename;
    }
    /**
     * Parse route data from the file and add all route to the route manager.
     *
     */
    public void parse() throws IOException, RouteDataMissingException, JSONException {
        DataProvider dataProvider = new FileDataProvider(filename);

        parseRoutes(dataProvider.dataSourceToString());
    }
    /**
     * Parse route information from JSON response produced by Translink.
     * Stores all routes and route patterns found in the RouteManager.
     *
     * @param  jsonResponse    string encoding JSON data to be parsed
     * @throws JSONException   when:
     * <ul>
     *     <li>JSON data does not have expected format (JSON syntax problem)
     *     <li>JSON data is not an array
     * </ul>
     * If a JSONException is thrown, no stops should be added to the stop manager
     *
     * @throws RouteDataMissingException when
     * <ul>
     *  <li>JSON data is missing RouteNo, Name, or Patterns element for any route</li>
     *  <li>The value of the Patterns element is not an array for any route</li>
     *  <li>JSON data is missing PatternNo, Destination, or Direction element for any route pattern</li>
     * </ul>
     * If a RouteDataMissingException is thrown, all correct routes are first added to the route manager.
     */

    public void parseRoutes(String jsonResponse)
            throws JSONException, RouteDataMissingException {

        JSONArray routes = new JSONArray(jsonResponse);

        for (int i = 0; i < routes.length(); i++) {
            JSONObject route = routes.getJSONObject(i);
            parseRoute(route);
        }
    }


    private void parseRoute(JSONObject route) throws JSONException, RouteDataMissingException {
        try {
            String routeName = route.getString("Name");
            String routeNum = route.getString("RouteNo");
            JSONArray patterns = route.getJSONArray("Patterns");

            Route tempRoute = RouteManager.getInstance().getRouteWithNumber(routeNum, routeName);
            tempRoute.setName(routeName);

            for (int i = 0; i < patterns.length(); i++) {
                try {
                    JSONObject pattern = patterns.getJSONObject(i); //notice the plural
                    String destination = pattern.getString("Destination");
                    String direction = pattern.getString("Direction");
                    String patternNum = pattern.getString("PatternNo");
                    RoutePattern rp = new RoutePattern(patternNum, destination, direction, tempRoute);
                    tempRoute.addPattern(rp);
                } catch (JSONException E) {
                    throw new RouteDataMissingException();
                }

            }

        } catch (JSONException E) {
            if (E.getMessage().equals("JSONObject[\"Name\"] not found.") || E.getMessage().equals("JSONObject[\"RouteNo\"] not found.")) {
                throw new RouteDataMissingException();
            }
            throw E;
        }
    }
}
