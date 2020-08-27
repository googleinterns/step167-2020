import React, { useState, useEffect } from "react";
import { Map, GoogleApiWrapper, Marker, InfoWindow } from "google-maps-react";
import PropTypes from "prop-types";
import RecipeCard from "./RecipeCard";
import { mapsApiKey } from "../requests";
import Geocode from "react-geocode";

const FeedMap = props => {
  const initialWindows = () => {
    let initial = [];
    props.recipes.forEach(recipe => {
      if (recipe.location && recipe.location.latitude && recipe.location.longitude) {
        initial.push({
          id: recipe.id,
          visible: false,
        });
      }
    });
    return initial;
  };

  const [windows, setWindows] = useState(initialWindows());
  const [center, setCenter] = useState(null);

  useEffect(() => {
    if (props.mapCenter !== "") {
      Geocode.setApiKey(mapsApiKey);
      Geocode.fromAddress(props.mapCenter).then(
        response => {
          let newCoords = null;
          if (response.results[0]) {
            // If there are results, traverse the array of address components
            // looking for a country, or, if none is found, a natural feature
            response.results.forEach(result => {
              console.log(result.formatted_address + JSON.stringify(result.types));
              if (
                result.types.includes("country") ||
                (result.types.includes("natural_feature") && newCoords === null)
              ) {
                newCoords = result.geometry.location;
              }
            });
          }
          if (newCoords === null) {
            newCoords = { lat: 0, lng: 0 };
          }
          setCenter(newCoords);
        },
        error => {
          console.log(error);
          setCenter({ lat: 0, lng: 0 });
        }
      );
    }
    setCenter({ lat: 0, lng: 0 });
  }, [props.mapCenter]);

  const getVisibility = recipeId => {
    if (!windows) return false;
    else {
      let window = windows.find(window => window.id === recipeId);
      if (window === undefined) return false;
      else return window.visible;
    }
  };

  const displayMarkers = () => {
    return props.recipes.map(recipe => {
      if (recipe.location && recipe.location.latitude && recipe.location.longitude) {
        return (
          <Marker
            title={recipe.title}
            id={recipe.id}
            key={recipe.id}
            position={{
              lat: recipe.location.latitude,
              lng: recipe.location.longitude,
            }}
            onClick={() => {
              setWindows(
                windows.map(window =>
                  window.id === recipe.id ? { id: window.id, visible: true } : { id: window.id, visible: false }
                )
              );
            }}
          ></Marker>
        );
      } else {
        return null;
      }
    });
  };

  const displayWindows = () => {
    return props.recipes.map(recipe => {
      if (recipe.location && recipe.location.latitude && recipe.location.longitude) {
        return (
          <InfoWindow
            key={recipe.title}
            visible={getVisibility(recipe.id)}
            options={{ maxWidth: 300 }}
            position={{
              lat: recipe.location.latitude,
              lng: recipe.location.longitude,
            }}
          >
            <div>
              <RecipeCard recipe={recipe} tags={props.tags} setErrMsg={props.setErrMsg} />
            </div>
          </InfoWindow>
        );
      } else {
        return null;
      }
    });
  };

  const mapClicked = (mapProps, map, clickEvent) => {
    map.panTo(clickEvent.latLng);
  };

  return (
    <Map google={props.google} zoom={props.mapCenter ? 4 : 2} style={mapStyles} center={center} onClick={mapClicked}>
      {displayMarkers()}
      {displayWindows()}
    </Map>
  );
};

FeedMap.propTypes = {
  recipes: PropTypes.array,
  google: PropTypes.object,
  tags: PropTypes.object,
  setErrMsg: PropTypes.func,
  mapCenter: PropTypes.string,
};

const mapStyles = {
  width: "95%",
  height: "85%",
};

export default GoogleApiWrapper({
  apiKey: mapsApiKey,
})(FeedMap);
