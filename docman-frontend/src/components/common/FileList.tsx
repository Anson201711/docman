'use client';

import { Table, Card, Tag, Space, Button, Tooltip, Dropdown, Empty } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  DownloadOutlined,
  DeleteOutlined,
  EditOutlined,
  MoreOutlined,
  EyeOutlined,
  FileOutlined,
  FolderOutlined,
} from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import type { Document, Folder } from '@/types';

interface FileListProps {
  dataSource: (Document | Folder)[];
  loading?: boolean;
  viewMode?: 'list' | 'grid';
  onDownload?: (record: Document) => void;
  onDelete?: (record: Document | Folder) => void;
  onEdit?: (record: Document | Folder) => void;
  onPreview?: (record: Document) => void;
  onRowClick?: (record: Document | Folder) => void;
}

const FileList: React.FC<FileListProps> = ({
  dataSource,
  loading = false,
  viewMode = 'list',
  onDownload,
  onDelete,
  onEdit,
  onPreview,
  onRowClick,
}) => {
  const { t } = useTranslation();

  const documentColumns: ColumnsType<Document> = [
    {
      title: t('documents.name'),
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <Space>
          <FileOutlined />
          <a onClick={() => onRowClick?.(record)}>{name}</a>
        </Space>
      ),
    },
    {
      title: t('documents.type'),
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => <Tag color="blue">{type}</Tag>,
    },
    {
      title: t('documents.size'),
      dataIndex: 'size',
      key: 'size',
      render: (size: number) => {
        if (size < 1024) return `${size} B`;
        if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
        if (size < 1024 * 1024 * 1024) return `${(size / (1024 * 1024)).toFixed(1)} MB`;
        return `${(size / (1024 * 1024 * 1024)).toFixed(1)} GB`;
      },
    },
    {
      title: t('documents.modified'),
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      render: (date: string) => new Date(date).toLocaleDateString(),
    },
    {
      title: '',
      key: 'actions',
      width: 150,
      render: (_, record) => (
        <Space size="small">
          <Tooltip title={t('common.download')}>
            <Button
              type="text"
              size="small"
              icon={<DownloadOutlined />}
              onClick={(e) => {
                e.stopPropagation();
                onDownload?.(record);
              }}
            />
          </Tooltip>
          <Tooltip title={t('common.edit')}>
            <Button
              type="text"
              size="small"
              icon={<EditOutlined />}
              onClick={(e) => {
                e.stopPropagation();
                onEdit?.(record);
              }}
            />
          </Tooltip>
          <Tooltip title={t('common.delete')}>
            <Button
              type="text"
              size="small"
              danger
              icon={<DeleteOutlined />}
              onClick={(e) => {
                e.stopPropagation();
                onDelete?.(record);
              }}
            />
          </Tooltip>
        </Space>
      ),
    },
  ];

  const folderColumns: ColumnsType<Folder> = [
    {
      title: t('folders.folderName'),
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <Space>
          <FolderOutlined />
          <a onClick={() => onRowClick?.(record)}>{name}</a>
        </Space>
      ),
    },
    {
      title: '',
      key: 'actions',
      width: 150,
      render: (_, record) => (
        <Space size="small">
          <Tooltip title={t('common.edit')}>
            <Button
              type="text"
              size="small"
              icon={<EditOutlined />}
              onClick={(e) => {
                e.stopPropagation();
                onEdit?.(record);
              }}
            />
          </Tooltip>
          <Tooltip title={t('common.delete')}>
            <Button
              type="text"
              size="small"
              danger
              icon={<DeleteOutlined />}
              onClick={(e) => {
                e.stopPropagation();
                onDelete?.(record);
              }}
            />
          </Tooltip>
        </Space>
      ),
    },
  ];

  const columns = [
    ...documentColumns,
    ...(folderColumns.length > 1 ? folderColumns.slice(1) : []),
  ];

  if (dataSource.length === 0 && !loading) {
    return (
      <Empty
        image={Empty.PRESENTED_IMAGE_SIMPLE}
        description={t('documents.noDocuments')}
        style={{ marginTop: 100 }}
      />
    );
  }

  return (
    <Table
      dataSource={dataSource as any[]}
      columns={columns}
      rowKey="id"
      loading={loading}
      pagination={{
        pageSize: 10,
        showSizeChanger: true,
        showTotal: (total) => `Total ${total} items`,
      }}
      onRow={(record) => ({
        onClick: () => onRowClick?.(record),
        style: { cursor: 'pointer' },
      })}
    />
  );
};

export default FileList;
