import React, { useState } from "react";
import { Map, GoogleApiWrapper, Marker, InfoWindow } from "google-maps-react";
import PropTypes from "prop-types";

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
            position={{
              lat: recipe.location.latitude,
              lng: recipe.location.longitude,
            }}
          >
            <div>
              <h1>{recipe.title}</h1>
            </div>
          </InfoWindow>
        );
      } else {
        return null;
      }
    });
  };

  const mapClicked = (mapProps, map, clickEvent) => {
    const newCenter = {
      lat: clickEvent.latLng.lat(),
      lng: clickEvent.latLng.lng(),
    };
    map.panTo(newCenter);
  };

  return (
    <Map google={props.google} zoom={2} style={mapStyles} initialCenter={{ lat: 0, lng: 0 }} onClick={mapClicked}>
      {displayMarkers()}
      {displayWindows()}
    </Map>
  );
};

FeedMap.propTypes = {
  recipes: PropTypes.array,
  google: PropTypes.object,
};

const mapStyles = {
  width: "97%",
  height: "100%",
};

export default GoogleApiWrapper({
  apiKey: "AIzaSyAe5HlFZFuhzMimXrKW1z3kglajbdHf_Rc",
})(FeedMap);
