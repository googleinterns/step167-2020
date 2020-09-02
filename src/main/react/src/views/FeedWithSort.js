import React, { useState } from "react";
import PropTypes from "prop-types";
import { CCol, CRow } from "@coreui/react";
import SortTypeSelect from "../components/SortTypeSelect";
import Feed from "./Feed";

const FeedWithSort = props => {
  const [sort, setSort] = useState(null);

  return (
    <>
      <CRow>
        <CCol>
          <SortTypeSelect sortType={sort} setSortType={setSort} />
        </CCol>
      </CRow>
      <br></br>
      <br></br>
      <Feed feedType={props.feedType} sortType={sort} />
    </>
  );
};

FeedWithSort.propTypes = {
  feedType: PropTypes.string,
};

export default FeedWithSort;
