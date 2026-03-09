import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './shared/ui/Layout';
import ReviewPage from './pages/ReviewPage';
import CollectionPage from './pages/CollectionPage';
import CrmPage from './pages/CrmPage';

export default function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<ReviewPage />} />
          <Route path="/collection" element={<CollectionPage />} />
          <Route path="/crm" element={<CrmPage />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}
